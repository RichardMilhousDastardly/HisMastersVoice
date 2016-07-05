package com.software.crucifix.hismastersvoice;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.pwittchen.swipe.library.Swipe;
import com.github.pwittchen.swipe.library.SwipeEvent;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.skyfishjy.library.RippleBackground;
import com.software.crucifix.hismastersvoice.listeners.BaseOnClickListener;
import com.software.crucifix.hismastersvoice.listeners.SongSpeechRecognitionListener;
import com.software.crucifix.hismastersvoice.listeners.ToonageBroadcastReceiver;
import com.software.crucifix.hismastersvoice.word.processing.Utility;

import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;

public class MainActivity extends AppCompatActivity {

    private final static String LOG_TAG = "HisMastersVoice";
    private final static int MY_PERMISSIONS_REQUEST_RECORD_AUDIO = 3746;
    private final static int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 3747;
    public final static String TOONAGE_FINISH_FILTER = "com.software.crucifix.hismastersvoice.finish";
    public final static String TOONAGE_NOW_PLAYING_FILTER = "com.software.crucifix.hismastersvoice.now.playing";
    public final static String TOONAGE_NOW_PLAYING_ALBUM = "com.software.crucifix.hismastersvoice.artist";
    public final static String TOONAGE_NOW_PLAYING_ARTIST = "com.software.crucifix.hismastersvoice.album";
    public final static String TOONAGE_NOW_PLAYING_SONG = "com.software.crucifix.hismastersvoice.song";

    private final static float FADE_OUT = 0.0f;
    private final static float FADE_IN = 1.0f;

    /**
     * Media Player Service
     */
    private BangingTunes mBangingTunes;
    private Intent mBangingTunesItent;
    private boolean mMusicBound = false;
    private ToonageBroadcastReceiver mToonageBroadcastReceiver;

    //connect to the BangingTunes background service
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(final ComponentName componentName, final IBinder service) {
            final BangingTunes.MusicBinder binder = (BangingTunes.MusicBinder) service;
            mBangingTunes = binder.getService();
            mBangingTunes.setSongList(null);
            mBangingTunes.manageAudioFocus();
            mMusicBound = true;
        }

        @Override
        public void onServiceDisconnected(final ComponentName componentName) {
            mMusicBound = false;
        }
    };

    /**
     * Swipe control
     */
    private Swipe mSwipe;
    private Subscription mSubscription;
    private AudioManager mAudioManager;


    /**
     * Async task to retrieve songs
     */
    private RetrieveSongs mRetrieveSongs;

    /**
     * Speech recognition
     */
    private SongSpeechRecognitionListener mSongSpeechRecognitionListener;
    private SpeechRecognizer mSpeechRecognizer = null;
    private Intent mRecognizerIntent;
    private final Utility mUtility = new Utility();

    private RippleBackground mRippleBackground;
    private HMVFloatingActionButton mFloatingActionButton;
    private TextView mAudioFound;
    private TextView mNowPlaying;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mRippleBackground = (RippleBackground) findViewById(R.id.raspberry_ripple);
        mAudioFound = (TextView) findViewById(R.id.audio_found);
        mAudioFound.setSelected(true);

        mNowPlaying = (TextView) findViewById(R.id.now_playing);
        mNowPlaying.setSelected(true);

        mToonageBroadcastReceiver = new ToonageBroadcastReceiver(this, mNowPlaying);
        registerReceiver(mToonageBroadcastReceiver, new IntentFilter(TOONAGE_FINISH_FILTER));
        registerReceiver(mToonageBroadcastReceiver, new IntentFilter(TOONAGE_NOW_PLAYING_FILTER));

        manageFAB();
        manageSpeechRecognition();
        manageSwipe();

        final Intent serviceIntent = new Intent(this, BangingTunes.class);
        serviceIntent.setAction(BangingTunes.ACTION_PLAY);
        startService(serviceIntent);

    }

    /**
     * Control track playing and volume by swiping
     */
    private void manageSwipe() {

        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mSwipe = new Swipe();

        mSubscription = mSwipe.observe()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<SwipeEvent>() {
                    @Override
                    public void call(final SwipeEvent swipeEvent) {

                        switch (swipeEvent) {
                            case SWIPING_DOWN:
                            case SWIPING_UP:
                            case SWIPING_LEFT:
                            case SWIPING_RIGHT:
                                break;
                            case SWIPED_DOWN:
                                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                                break;
                            case SWIPED_UP:
                                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                                break;
                            case SWIPED_LEFT:
                                mBangingTunes.previous();
                                break;
                            case SWIPED_RIGHT:
                                mBangingTunes.next();
                                break;
                            default:
                                break;
                        }
                    }
                });
    }

    @Override
    public boolean dispatchTouchEvent(final MotionEvent motionEvent) {
        mSwipe.dispatchTouchEvent(motionEvent);
        return super.dispatchTouchEvent(motionEvent);
    }

    /**
     * @param permission
     * @param myPermissionsRequestId
     * @param explanation
     * @return is permission granted
     */
    private boolean havePermission(final String permission, final int myPermissionsRequestId, final String explanation) {
        final int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, permission);

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showMessageOKCancel(explanation, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(final DialogInterface dialog, final int which) {
                            requestPermissions(new String[]{permission}, myPermissionsRequestId);
                        }
                    });
                    return false;
                }
            } else {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, myPermissionsRequestId);
            }
        }

        return false;
    }

    private void showMessageOKCancel(final String message, final DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }


    /**
     * This application is IMMERSIVE
     *
     * @param hasFocus
     */
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        final View decorView = getWindow().getDecorView();

        if (hasFocus) {
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    /**
     * Its all controlled by a FAB
     */
    private void manageFAB() {
        final ImageView audioTrack = new ImageView(this);
        audioTrack.setImageResource(R.drawable.audiotrack);

        mFloatingActionButton = new HMVFloatingActionButton.Builder(this).setPosition(HMVFloatingActionButton.POSITION_CENTER_CENTER).setContentView(audioTrack).build();

        final SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);

        //PAUSE
        final ImageView pause = new ImageView(this);
        pause.setImageResource(R.drawable.pause);
        final SubActionButton pauseButton = itemBuilder.setContentView(pause).build();
        pauseButton.setOnClickListener(buildPauseOnClickListener());

        //PLAY
        final ImageView play = new ImageView(this);
        play.setImageResource(R.drawable.play);
        final SubActionButton playButton = itemBuilder.setContentView(play).build();
        playButton.setOnClickListener(buildPlayOnClickListener());

        //REPEAT ONE
        final ImageView repeatOne = new ImageView(this);
        repeatOne.setImageResource(R.drawable.repeat_one);
        final SubActionButton repeatOneButton = itemBuilder.setContentView(repeatOne).build();
        repeatOneButton.setOnClickListener(buildRepeatOneOnClickListener());

        //HEARING
        final ImageView hearing = new ImageView(this);
        hearing.setImageResource(R.drawable.hearing);
        final SubActionButton hearingButton = itemBuilder.setContentView(hearing).build();
        hearingButton.setOnClickListener(buildHearingOnClickListener());

        //SHUFFLE
        final ImageView shuffle = new ImageView(this);
        shuffle.setImageResource(R.drawable.shuffle);
        final SubActionButton shuffleButton = itemBuilder.setContentView(shuffle).build();
        shuffleButton.setOnClickListener(buildShuffleOnClickListener());

        //SKIP NEXT
        final ImageView skipNext = new ImageView(this);
        skipNext.setImageResource(R.drawable.skip_next);
        final SubActionButton skipNextButton = itemBuilder.setContentView(skipNext).build();
        skipNextButton.setOnClickListener(buildSkipNextOnClickListener());

        //SKIP PREVIOUS
        final ImageView skipPrevious = new ImageView(this);
        skipPrevious.setImageResource(R.drawable.skip_previous);
        final SubActionButton skipPreviousButton = itemBuilder.setContentView(skipPrevious).build();
        skipPreviousButton.setOnClickListener(buildSkipPreviousOnClickListener());

        //STOP
        final ImageView stop = new ImageView(this);
        stop.setImageResource(R.drawable.stop);
        final SubActionButton stopButton = itemBuilder.setContentView(stop).build();
        stopButton.setOnClickListener(buildStopOnClickListener());

        //POWER
        final ImageView power = new ImageView(this);
        power.setImageResource(R.drawable.power);
        final SubActionButton powerButton = itemBuilder.setContentView(power).build();
        powerButton.setOnClickListener(buildPowerOnClickListener());

        //INFORMATION
        final ImageView information = new ImageView(this);
        information.setImageResource(R.drawable.information);
        final SubActionButton informationButton = itemBuilder.setContentView(information).build();
        informationButton.setOnClickListener(buildInformationOnClickListener());

        final FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(informationButton)
                .addSubActionView(shuffleButton)
                .addSubActionView(playButton)
                .addSubActionView(stopButton)
                .addSubActionView(hearingButton)
                .addSubActionView(pauseButton)
                .addSubActionView(skipNextButton)
                .addSubActionView(skipPreviousButton)
                .addSubActionView(repeatOneButton)
                .addSubActionView(powerButton)
                .attachTo(mFloatingActionButton).setStartAngle(30).setEndAngle(330).build();

    }

    /**
     * FAB On Click Listeners
     */

    private View.OnClickListener buildPauseOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.pause();
            }
        };
    }

    private View.OnClickListener buildPlayOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.play();

            }
        };
    }

    private View.OnClickListener buildRepeatOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.repeat();
            }
        };
    }

    private View.OnClickListener buildRepeatOneOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.repeatOnce();
            }
        };
    }

    private View.OnClickListener buildHearingOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);

                if (havePermission(RECORD_AUDIO, MY_PERMISSIONS_REQUEST_RECORD_AUDIO, getResources().getString(R.string.permission_explanation_record_audio))) {
                    havePermission(READ_EXTERNAL_STORAGE, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE, getResources().getString(R.string.permission_explanation_read_external_storage));
                }

                mBangingTunes.stop();
                mFloatingActionButton.setEnabled(false);
                mRetrieveSongs = new RetrieveSongs(MainActivity.this);
                mSongSpeechRecognitionListener.resetIgnore();
                mSongSpeechRecognitionListener.setAsyncTask(mRetrieveSongs);
                mSpeechRecognizer.startListening(mRecognizerIntent);
            }
        };
    }

    private View.OnClickListener buildShuffleOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.shuffle();
            }
        };
    }

    private View.OnClickListener buildSkipNextOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.next();
            }
        };
    }

    private View.OnClickListener buildSkipPreviousOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.previous();
            }
        };
    }

    private View.OnClickListener buildStopOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                mBangingTunes.stop();
            }
        };
    }

    private View.OnClickListener buildPowerOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);
                MainActivity.this.finish();
            }
        };
    }

    private View.OnClickListener buildInformationOnClickListener() {
        return new BaseOnClickListener(mFloatingActionButton) {
            @Override
            public void onClick(final View view) {
                super.onClick(view);

                final LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
                final View informationView = layoutInflater.inflate(R.layout.information, null);

                final AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainActivity.this);
                dialogBuilder.setView(informationView);

                final AlertDialog alertDialog = dialogBuilder.create();

                final ImageView tickButton = (ImageView) informationView.findViewById(R.id.information_button);
                tickButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View view) {
                        alertDialog.cancel();
                    }
                });

                alertDialog.show();
            }
        };
    }


    /**
     * Configure the speech recognition
     */
    private void manageSpeechRecognition() {

        mRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, Locale.getDefault().getLanguage().trim());
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 100);

        mRecognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        mSongSpeechRecognitionListener = new SongSpeechRecognitionListener(mRippleBackground, mFloatingActionButton);

        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mSpeechRecognizer.setRecognitionListener(mSongSpeechRecognitionListener);

    }

    /**
     * When the Activity start, we start the Background Media Player service
     */
    @Override
    protected void onStart() {
        super.onStart();

        if (mBangingTunesItent == null) {
            mBangingTunesItent = new Intent(this, BangingTunes.class);
            final boolean bindIndicator = bindService(mBangingTunesItent, mServiceConnection, Context.BIND_AUTO_CREATE);
            final ComponentName componentName = startService(mBangingTunesItent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {

        if (mSubscription != null && !mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }

        if (mSpeechRecognizer == null) {
        } else {
            mSpeechRecognizer.destroy();
        }

        if (mRetrieveSongs == null) {
        } else {
            mRetrieveSongs.cancel(true);
        }


        if (mBangingTunesItent == null) {
        } else {
            mBangingTunes.manageAbandonAudioFocus();
            unregisterReceiver(mToonageBroadcastReceiver);
            mBangingTunes.powerOff();
        }

        if (mServiceConnection == null) {
        } else {
            unbindService(mServiceConnection);
        }

        stopService(mBangingTunesItent);
        mBangingTunes = null;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    /**
     * Has the user allowed the permission
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, final String permissions[], final int[] grantResults) {

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_RECORD_AUDIO: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    this.finish();
                }
                return;
            }
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    recreate();
                } else {
                    this.finish();
                }
                return;
            }
        }
    }

    /**
     * Get all songs in background task
     */
    private class RetrieveSongs extends AsyncTask<String, Void, Map<TooonageVO, String>> {

        private final ProgressDialog mProgressDialog;
        private final Context mContext;
        private String voiceRecognitioned = "";

        private final Map<TooonageVO, String> tooons = new TreeMap<>();

        public RetrieveSongs(final Context context) {
            mContext = context;
            mProgressDialog = new ProgressDialog(mContext);
        }

        @Override
        protected void onPreExecute() {
            tooons.clear();
            if (mSpeechRecognizer == null) {
            } else {
                try {
                    Log.i(LOG_TAG, "mSpeechRecognizer.cancel()");
                    mSpeechRecognizer.cancel();
                } catch (final Exception ignoreException) {
                    Log.i(LOG_TAG, "FAILED to stop mSpeechRecognizer");
                }
            }
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected Map<TooonageVO, String> doInBackground(final String... params) {
            return getAllSongsFromSDCARD(params[0]);
        }

        /**
         * Read all songs and check Artist, Album, and Song
         */
        private Map<TooonageVO, String> getAllSongsFromSDCARD(final String voice) {
            voiceRecognitioned = voice;

            if (isCancelled()) {
                return null;
            }

            final String[] TOOONAGE_PROJECTION = new String[]{MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST};
            final Uri allSongsUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            final String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            final Cursor cursor = getContentResolver().query(allSongsUri, TOOONAGE_PROJECTION, selection, null, null);

            try {

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {

                            if (isCancelled()) {
                                return null;
                            }

                            final int songId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media._ID));
                            final String songName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));

                            final String songPath = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));

                            final int albumId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                            final String albumName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM));

                            final int artistId = cursor.getInt(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));
                            final String artistName = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));

                            if (mUtility.match(artistName, voice) | mUtility.match(albumName, voice) | mUtility.match(songName, voice)) {
                                final TooonageVO tooonageVO = new TooonageVO.Builder().artist(artistName).albumn(albumName).song(songName).build();
                                tooons.put(tooonageVO, songPath.trim());
                            }

                        } while (cursor.moveToNext());
                    }
                }
            } finally {
                cursor.close();
            }

            return tooons;
        }

        /**
         * <p>Runs on the UI thread after {@link #doInBackground}. The
         * specified result is the value returned by {@link #doInBackground}.</p>
         * <p>
         * <p>This method won't be invoked if the task was cancelled.</p>
         *
         * @param tooons The result of the operation computed by {@link #doInBackground}.
         * @see #onPreExecute
         * @see #doInBackground
         * @see #onCancelled(Object)
         */
        @Override
        protected void onPostExecute(final Map<TooonageVO, String> tooons) {

            if (isCancelled()) {
                return;
            }

            mRippleBackground.animate().alpha(FADE_OUT).setDuration(2000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mRippleBackground.stopRippleAnimation();
                    mRippleBackground.setAlpha(FADE_IN);
                }
            });

            mAudioFound.setText(voiceRecognitioned);
            mAudioFound.animate().alpha(FADE_IN).setDuration(10000).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mAudioFound.animate().alpha(FADE_OUT).setDuration(10000);
                }
            });

            if (tooons == null || tooons.isEmpty()) {
            } else {
                mBangingTunes.setSongList(tooons);
                mBangingTunes.play();
            }
        }
    }
}