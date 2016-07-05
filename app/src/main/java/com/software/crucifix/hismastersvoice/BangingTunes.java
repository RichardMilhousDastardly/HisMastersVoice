package com.software.crucifix.hismastersvoice;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Rating;
import android.media.session.MediaController;
import android.media.session.MediaSession;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.software.crucifix.hismastersvoice.enums.SONG_INDEX;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by MUTTLEY on 30/05/2016.
 */
public class BangingTunes extends Service implements AudioControllable, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    /**
     * LOCK screen notification actions
     */
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_POWER = "action_power";

    /**
     * Constants
     */
    private final static String LOG_TAG = "BangingTunes";
    private final static String FILE_SCHEME = "file://";
    private final static Map<TooonageVO, String> mSongList = new TreeMap<>();
    private final static List<TooonageVO> mSongKeys = new ArrayList<>();
    private final static List<TooonageVO> mShuffleSongKeys = new ArrayList<>();
    private final static Map<TooonageVO, String> mOriginalSongList = new TreeMap<>();

    private AudioManager mAudioManager;

    private MediaPlayer mMediaPlayer = new MediaPlayer();
    private MediaSessionManager mMediaSessionManager;
    private MediaSession mMediaSession;
    private MediaController mMediaController;

    private boolean shuffle = false;
    private boolean repeat = false;
    private boolean paused = false;

    private final IBinder mBangingTunesBinder = new MusicBinder();
    private int mSongIndex = 0;

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(final Intent intent) {
        return mBangingTunesBinder;
    }

    /**
     * Release the media player resources
     *
     * @param intent
     * @return
     */
    @Override
    public boolean onUnbind(final Intent intent) {
        mMediaPlayer.stop();
        mMediaPlayer.reset();
        mMediaPlayer.release();
        return false;
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        if (mMediaSessionManager == null) {
            initMediaSessions();
        }

        handleIntent(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        resetSongLists();
        initialiseMediaPlayer();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
    }

    /**
     * Configure the mediaPlayer to keep playing etc...
     */
    private void initialiseMediaPlayer() {
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
    }

    /**
     * Banging Toooons is controllerable
     */
    @Override
    public void play() {
        if (mSongList.isEmpty()) {
            return;
        }

        manageSongIndex(SONG_INDEX.PRESERVE);

        final TooonageVO tooonageVO = mSongKeys.get(mSongIndex);

        final Uri songUri = Uri.parse(FILE_SCHEME + mSongList.get(tooonageVO));

        try {
            if (paused && (mMediaPlayer.getCurrentPosition() > 1)) {

                paused = !paused;
                if (mMediaPlayer.isPlaying()) {
                } else {
                    final int pausedPosition = mMediaPlayer.getCurrentPosition();
                    mMediaPlayer.seekTo(pausedPosition);
                    mMediaPlayer.start();
                    return;
                }
            }

            if (mMediaPlayer.isLooping()) {
            } else {
                mMediaPlayer.reset();
            }

            mMediaPlayer.setDataSource(getApplicationContext(), songUri);
            mMediaPlayer.prepareAsync();

            final Intent nowPlayingIntent = new Intent(MainActivity.TOONAGE_NOW_PLAYING_FILTER);
            nowPlayingIntent.putExtra(MainActivity.TOONAGE_NOW_PLAYING_ARTIST, tooonageVO.getArtist());
            nowPlayingIntent.putExtra(MainActivity.TOONAGE_NOW_PLAYING_ALBUM, tooonageVO.getAlbumn());
            nowPlayingIntent.putExtra(MainActivity.TOONAGE_NOW_PLAYING_SONG, tooonageVO.getSong());

            sendBroadcast(nowPlayingIntent);
        } catch (final Exception exception) {
            Log.e(LOG_TAG, "playBangingTunes()", exception);
        } finally {
            buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE));
        }
    }

    @Override
    public void pause() {

        if (mSongList.isEmpty()) {
            return;
        }

        manageSongIndex(SONG_INDEX.PRESERVE);

        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                paused = true;
                buildNotification(generateAction(R.drawable.play, "Play", ACTION_PLAY));
            }
        } catch (final Exception exception) {
            Log.e(LOG_TAG, "Media Player Pause failed", exception);
        }

    }

    @Override
    public void stop() {

        if (mSongList.isEmpty()) {
            return;
        }

        manageSongIndex(SONG_INDEX.PRESERVE);

        try {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
                buildNotification(generateAction(R.drawable.play, "Play", ACTION_PLAY));
            }
        } catch (final Exception exception) {
            Log.e(LOG_TAG, "Media Player Stop failed", exception);
        }

    }

    @Override
    public void next() {

        manageSongIndex(SONG_INDEX.INCREMENT);

        play();
    }

    @Override
    public void previous() {

        manageSongIndex(SONG_INDEX.DECREMENT);

        play();

    }

    @Override
    public void repeat() {

        if (mSongList.isEmpty()) {
            return;
        }

        manageSongIndex(SONG_INDEX.PRESERVE);

        repeat = !repeat;

        mMediaPlayer.setLooping(repeat);
    }

    @Override
    public void repeatOnce() {

        repeat();
    }

    @Override
    public void shuffle() {

        if (mSongList.isEmpty()) {
            return;
        }

        manageSongIndex(SONG_INDEX.PRESERVE);

        if (shuffle) {
            try {
                managePlayList(new ArrayList<TooonageVO>(mOriginalSongList.keySet()));
            } catch (final Exception exception) {
                Log.e(LOG_TAG, "Media Player Shuffle reset failed", exception);
            }
        } else {
            try {
                managePlayList(mShuffleSongKeys);
            } catch (final Exception exception) {
                Log.e(LOG_TAG, "Media Player Shuffle failed", exception);
            }
        }

        shuffle = !shuffle;

    }

    /**
     * Manage the song Index value
     *
     * @param songIndex
     */
    private void manageSongIndex(final SONG_INDEX songIndex) {

        switch (songIndex) {
            case DECREMENT:
                if (mSongIndex == 0) {
                    mSongIndex = mSongList.size();
                }
                mSongIndex--;
                break;
            case INCREMENT:
                if (mSongIndex == (mSongList.size() - 1)) {
                    mSongIndex = 0;
                } else {
                    mSongIndex++;
                }
                break;
            case PRESERVE:
                break;
        }

    }

    private void managePlayList(final List<TooonageVO> songKeys) {
        mSongIndex = 0;
        mSongKeys.clear();
        mSongKeys.addAll(songKeys);

        play();
    }


    @Override
    public boolean manageAudioFocus() {
        final int AUDIO_FOCUS_RESULT = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        if (AUDIO_FOCUS_RESULT == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            return true;
        }

        return false;
    }

    @Override
    public boolean manageAbandonAudioFocus() {
        mAudioManager.abandonAudioFocus(this);
        return true;
    }

    /**
     * Store all songs that have been found
     *
     * @param playList
     */
    public void setSongList(final Map<TooonageVO, String> playList) {
        resetSongLists();

        if (playList == null) {
        } else {
            mSongList.putAll(playList);
            mOriginalSongList.putAll(mSongList);

            final List<TooonageVO> keys = new ArrayList(mSongList.keySet());
            mSongKeys.addAll(keys);

            Collections.shuffle(keys);

            mShuffleSongKeys.addAll(keys);
        }
    }

    /**
     * Reset everything associated with play back.
     */
    private void resetSongLists() {
        mSongList.clear();
        mSongKeys.clear();
        mShuffleSongKeys.clear();
        mOriginalSongList.clear();
        mSongIndex = 0;
        shuffle = false;
        repeat = false;
        paused = false;

        try {
            mMediaPlayer.reset();
        } catch (final Exception ignoreException) {
        }
    }

    /**
     * Called on the listener to notify it the audio focus for this listener has been changed.
     * The focusChange value indicates whether the focus was gained,
     * whether the focus was lost, and whether that loss is transient, or whether the new focus
     * holder will hold it for an unknown amount of time.
     * When losing focus, listeners can use the focus change information to decide what
     * behavior to adopt when losing focus. A music player could for instance elect to lower
     * the volume of its music stream (duck) for transient focus losses, and pause otherwise.
     *
     * @param focusChange the type of focus change, one of {@link AudioManager#AUDIOFOCUS_GAIN},
     *                    {@link AudioManager#AUDIOFOCUS_LOSS}, {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT}
     *                    and {@link AudioManager#AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK}.
     */
    @Override
    public void onAudioFocusChange(final int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                stop();
                break;
        }
    }

    /**
     *
     */
    public class MusicBinder extends Binder {

        public BangingTunes getService() {
            return BangingTunes.this;
        }
    }

    /**
     *
     * M E D I A  P L A Y E R
     *
     *
     */
    /**
     * Called when the end of a media source is reached during playback.
     *
     * @param mediaPlayer the MediaPlayer that reached the end of the file
     */
    @Override
    public void onCompletion(final MediaPlayer mediaPlayer) {
        try {


            if (mMediaPlayer.isLooping()) {
            } else {
                manageSongIndex(SONG_INDEX.INCREMENT);
            }

            play();

        } catch (final Exception exception) {
            Log.e(LOG_TAG, "MediaPlayer.OnCompletionListener mediaPlayer.reset()", exception);
        }
    }

    /**
     * Called to indicate an error.
     *
     * @param mediaPlayer the MediaPlayer the error pertains to
     * @param what        the type of error that has occurred:
     *                    <ul>
     *                    </ul>
     * @param extra       an extra code, specific to the error. Typically
     *                    implementation dependent.
     *                    <ul>
     *                    </ul>
     * @return True if the method handled the error, false if it didn't.
     * Returning false, or not having an OnErrorListener at all, will
     * cause the OnCompletionListener to be called.
     */
    @Override
    public boolean onError(final MediaPlayer mediaPlayer, final int what, final int extra) {

        final String whatMessage;
        final String extraMessage;

        switch (what) {
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                whatMessage = "MEDIA_ERROR_UNKNOWN";
                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                whatMessage = "MEDIA_ERROR_SERVER_DIED";
                break;
            default:
                whatMessage = "MEDIA_ERROR_DEFAULT_WHAT";
                break;
        }

        switch (extra) {
            case MediaPlayer.MEDIA_ERROR_IO:
                extraMessage = "MEDIA_ERROR_IO";
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                extraMessage = "MEDIA_ERROR_MALFORMED";
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                extraMessage = "MEDIA_ERROR_UNSUPPORTED";
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                extraMessage = "MEDIA_ERROR_TIMED_OUT";
                break;
            default:
                extraMessage = "MEDIA_ERROR_DEFAULT_EXTRA";
                break;
        }

        Log.e(LOG_TAG, "MediaPlayer.OnErrorListener whatMessage " + whatMessage + " - " + extraMessage);

        try {
            mediaPlayer.reset();
        } catch (final Exception exception) {
            Log.e(LOG_TAG, "MediaPlayer.OnErrorListener mediaPlayer.reset()", exception);
        }

        return false;
    }

    /**
     * Called when the media file is ready for playback.
     *
     * @param mediaPlayer the MediaPlayer that is ready for playback
     */
    @Override
    public void onPrepared(final MediaPlayer mediaPlayer) {
        mediaPlayer.start();
    }

    /**
     * L O C K  S C R E E N
     */
    private void handleIntent(final Intent intent) {

        if ((intent == null) || (intent.getAction() == null))
            return;

        final String action = intent.getAction();

        switch (action) {
            case ACTION_POWER:
                powerOff();
                break;
            case ACTION_PLAY:
                mMediaController.getTransportControls().play();
                break;
            case ACTION_PAUSE:
                mMediaController.getTransportControls().pause();
                break;
            case ACTION_PREVIOUS:
                mMediaController.getTransportControls().skipToPrevious();
                break;
            case ACTION_NEXT:
                mMediaController.getTransportControls().skipToNext();
                break;
            case ACTION_STOP:
                mMediaController.getTransportControls().stop();
                break;
            default:
                mMediaController.getTransportControls().stop();
                break;
        }
    }

    /**
     * The service and the activity use this to power off the media player
     */
    public void powerOff() {
        final NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);
        final Intent serviceIntent = new Intent(getApplicationContext(), BangingTunes.class);
        stopService(serviceIntent);
        sendBroadcast(new Intent(MainActivity.TOONAGE_FINISH_FILTER));
    }


    /**
     * @param icon
     * @param title
     * @param intentAction
     * @return
     */
    private Notification.Action generateAction(final int icon, final String title, final String intentAction) {
        final Intent intent = new Intent(getApplicationContext(), BangingTunes.class);
        intent.setAction(intentAction);

        final Icon iconDrawable = Icon.createWithResource(this, icon);
        final PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new Notification.Action.Builder(iconDrawable, title, pendingIntent).build();
    }

    /**
     * Present the notification to control the media playback
     *
     * @param action
     */
    private void buildNotification(final Notification.Action action) {
        final Notification.MediaStyle mediaStyle = new Notification.MediaStyle();

        final Intent intent = new Intent(getApplicationContext(), BangingTunes.class);
        intent.setAction(ACTION_POWER);

        final PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        final Notification.Builder builder = new Notification.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notif)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText("Say It, Play It!")
                .setOngoing(true)
                .setDeleteIntent(pendingIntent)
                .setStyle(mediaStyle);

        builder.addAction(generateAction(R.drawable.stop, "Stop", ACTION_STOP));
        builder.addAction(generateAction(R.drawable.skip_previous, "Previous", ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(R.drawable.skip_next, "Next", ACTION_NEXT));
        builder.addAction(generateAction(R.drawable.power, "Power", ACTION_POWER));
        mediaStyle.setShowActionsInCompactView(0, 1, 2, 3, 4);

        final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, builder.build());
    }

    /**
     * Initialise the media session and associate it with notification
     */
    private void initMediaSessions() {

        mMediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        mMediaSession = new MediaSession(getApplicationContext(), "Banging Toooons");
        mMediaController = new MediaController(getApplicationContext(), mMediaSession.getSessionToken());

        mMediaSession.setCallback(new MediaSession.Callback() {
                                      @Override
                                      public void onPlay() {
                                          super.onPlay();
                                          BangingTunes.this.play();
                                          buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE));
                                      }

                                      @Override
                                      public void onPause() {
                                          super.onPause();
                                          BangingTunes.this.pause();
                                          buildNotification(generateAction(R.drawable.play, "Play", ACTION_PLAY));
                                      }

                                      @Override
                                      public void onSkipToNext() {
                                          super.onSkipToNext();
                                          BangingTunes.this.next();
                                          buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE));
                                      }

                                      @Override
                                      public void onSkipToPrevious() {
                                          super.onSkipToPrevious();
                                          BangingTunes.this.previous();
                                          buildNotification(generateAction(R.drawable.pause, "Pause", ACTION_PAUSE));
                                      }

                                      @Override
                                      public void onFastForward() {
                                          super.onFastForward();
                                          BangingTunes.this.next();
                                      }

                                      @Override
                                      public void onRewind() {
                                          super.onRewind();
                                      }

                                      @Override
                                      public void onStop() {
                                          super.onStop();
                                          BangingTunes.this.stop();
                                          buildNotification(generateAction(R.drawable.play, "Play", ACTION_PLAY));
                                      }

                                      @Override
                                      public void onSeekTo(long pos) {
                                          super.onSeekTo(pos);
                                      }

                                      @Override
                                      public void onSetRating(Rating rating) {
                                          super.onSetRating(rating);
                                      }
                                  }
        );
    }
}