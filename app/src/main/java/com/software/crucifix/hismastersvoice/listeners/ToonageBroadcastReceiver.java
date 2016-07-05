package com.software.crucifix.hismastersvoice.listeners;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.TextView;

import com.software.crucifix.hismastersvoice.MainActivity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.Locale;

/**
 * Created by MUTTLEY on 24/06/2016.
 */
public class ToonageBroadcastReceiver extends BroadcastReceiver {

    private static final String EMPTY = "";
    private static final String[] AUDIO_FORMAT = new String[]{"mp3", "flac", "wav", "3gp", "mp4", "m4a", "aac", "ogg", "mkv"};

    private final Context mContext;
    private final TextView mNowPlaying;


    public ToonageBroadcastReceiver(final Context context, final TextView nowPlaying) {
        this.mContext = context;
        this.mNowPlaying = nowPlaying;
    }

    /**
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(final Context context, final Intent intent) {

        final String action = intent.getAction();
        Log.i("hismastersvoice", "intent = " + action);

        switch (action) {
            case MainActivity.TOONAGE_FINISH_FILTER:
                ((Activity) this.mContext).finish();
                break;
            case MainActivity.TOONAGE_NOW_PLAYING_FILTER:
                final String artist = preProcess(intent.getStringExtra(MainActivity.TOONAGE_NOW_PLAYING_ARTIST));
                final String album = preProcess(intent.getStringExtra(MainActivity.TOONAGE_NOW_PLAYING_ALBUM));
                final String song = preProcess(intent.getStringExtra(MainActivity.TOONAGE_NOW_PLAYING_SONG));
                this.mNowPlaying.setText(artist + " * " + album + " * " + song);
                break;
            default:
                break;
        }
    }


    private String preProcess(final String rawdata) {

        if (rawdata == null) {
            return EMPTY;
        }

        final String trimmedRawData = StringUtils.normalizeSpace(rawdata);

        if (trimmedRawData.isEmpty()) {
            return EMPTY;
        }

        final String lowerCaseRawData = trimmedRawData.toLowerCase(Locale.getDefault());

        for (final String format : AUDIO_FORMAT) {
            if (lowerCaseRawData.endsWith(format)) {
                final String replaced = lowerCaseRawData.replace(format, EMPTY);
                return WordUtils.capitalizeFully(replaced);
            }
        }

        return WordUtils.capitalizeFully(lowerCaseRawData);
    }
}
