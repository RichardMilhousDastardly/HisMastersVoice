package com.software.crucifix.hismastersvoice.listeners;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.skyfishjy.library.RippleBackground;
import com.software.crucifix.hismastersvoice.TooonageVO;

import java.util.List;
import java.util.Map;

/**
 * Created by MUTTLEY on 30/05/2016.
 */
public class SongSpeechRecognitionListener extends BaseSpeechRecognitionListener {

    private final FloatingActionButton mFloatingActionButton;
    private AsyncTask<String, Void, Map<TooonageVO, String>> mAsyncTask;

    public SongSpeechRecognitionListener(final RippleBackground rippleBackground, final FloatingActionButton floatingActionButton) {
        super(rippleBackground);
        this.mFloatingActionButton = floatingActionButton;
    }

    public void setAsyncTask(final AsyncTask<String, Void, Map<TooonageVO, String>> asyncTask) {
        this.mAsyncTask = asyncTask;
    }

    /**
     * Called when partial recognition results are available. The callback might be called at any
     * time between {@link #onBeginningOfSpeech()} and {@link #onResults(Bundle)} when partial
     * results are ready. This method may be called zero, one or multiple times for each call to
     * {@link SpeechRecognizer#startListening(Intent)}, depending on the speech recognition
     * service implementation.  To request partial results, use
     * {@link RecognizerIntent#EXTRA_PARTIAL_RESULTS}
     *
     * @param partialResults the returned results. To retrieve the results in
     *                       ArrayList&lt;String&gt; format use {@link Bundle#getStringArrayList(String)} with
     *                       {@link SpeechRecognizer#RESULTS_RECOGNITION} as a parameter
     */
    @Override
    public void onPartialResults(final Bundle partialResults) {
        Log.i(LOG_TAG, "onPartialResults()");

        final List<String> albumPartialResultList = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        for (final String result : albumPartialResultList) {

            if (result.isEmpty()) {
            } else {
                mPartialResults++;
                if (mPartialResults == mPartialResultsMAX) {
                    Log.i(LOG_TAG, "onPartialResults() EXECUTE");
                    mFloatingActionButton.setEnabled(true);
                    mAsyncTask.execute(result);
                    break;
                }
            }
        }
    }

    /**
     * Called when recognition results are ready.
     *
     * @param results the recognition results. To retrieve the results in {@code
     *                ArrayList&lt;String&gt;} format use {@link Bundle#getStringArrayList(String)} with
     *                {@link SpeechRecognizer#RESULTS_RECOGNITION} as a parameter. A float array of
     *                confidence values might also be given in {@link SpeechRecognizer#CONFIDENCE_SCORES}.
     */
    @Override
    public void onResults(final Bundle results) {

        if (mPartialResults > mPartialResultsLIMIT) {
        } else {
            final List<String> albumResultList = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            mFloatingActionButton.setEnabled(true);
            mAsyncTask.execute(albumResultList.get(0));
        }
    }

    /**
     * A network or recognition error occurred.
     *
     * @param errorCode code is defined in {@link SpeechRecognizer}
     */
    @Override
    public void onError(final int errorCode) {
        final String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);

        if (mSpeechRecognizerIgnore) {
            if (errorCode == SpeechRecognizer.ERROR_NO_MATCH) {
                return;
            }
        }

        mFloatingActionButton.setEnabled(true);
        mRippleBackground.animate().alpha(FADE_OUT).setDuration(2000).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mRippleBackground.stopRippleAnimation();
                mRippleBackground.setAlpha(FADE_IN);
            }
        });
    }
}
