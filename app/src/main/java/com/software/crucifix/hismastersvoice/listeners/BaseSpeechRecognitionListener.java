package com.software.crucifix.hismastersvoice.listeners;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import com.skyfishjy.library.RippleBackground;

import java.util.List;

/**
 * Created by MUTTLEY on 30/05/2016.
 */
public abstract class BaseSpeechRecognitionListener implements RecognitionListener {

    protected final static String LOG_TAG = "HisMastersVoice";
    protected final static float FADE_OUT = 0.0f;
    protected final static float FADE_IN = 1.0f;

    protected boolean mSpeechRecognizerIgnore = false;
    protected final RippleBackground mRippleBackground;

    protected static final int mPartialResultsMAX = 2;
    protected static final int mPartialResultsLIMIT = mPartialResultsMAX - 1;
    protected int mPartialResults = 0;



    /**
     * @param rippleBackground
     */
    public BaseSpeechRecognitionListener(final RippleBackground rippleBackground) {
        this.mRippleBackground = rippleBackground;
        this.mSpeechRecognizerIgnore = true;
    }

    /**
     *
     */
    public void resetIgnore() {
        mSpeechRecognizerIgnore = true;
    }

    /**
     * The user has started to speak.
     */
    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech()");
        mPartialResults = 0;
    }

    /**
     * The sound level in the audio stream has changed. There is no guarantee that this method will
     * be called.
     *
     * @param rmsdB the new RMS dB value
     */
    @Override
    public void onRmsChanged(final float rmsdB) {
    }

    /**
     * More sound has been received. The purpose of this function is to allow giving feedback to the
     * user regarding the captured audio. There is no guarantee that this method will be called.
     *
     * @param buffer a buffer containing a sequence of big-endian 16-bit integers representing a
     *               single channel audio stream. The sample rate is implementation dependent.
     */
    @Override
    public void onBufferReceived(final byte[] buffer) {
        Log.i(LOG_TAG, "onBufferReceived()");
    }

    /**
     * Called when the endpointer is ready for the user to start speaking.
     *
     * @param params parameters set by the recognition service. Reserved for future use.
     */
    @Override
    public void onReadyForSpeech(final Bundle params) {
        Log.i(LOG_TAG, "onReadyForSpeech()");
        mSpeechRecognizerIgnore = false;
        mRippleBackground.startRippleAnimation();
    }

    /**
     * Reserved for adding future events.
     *
     * @param eventType the type of the occurred event
     * @param params    a Bundle containing the passed parameters
     */
    @Override
    public void onEvent(final int eventType, final Bundle params) {
        Log.i(LOG_TAG, "onEvent()");
    }

    /**
     * Called after the user stops speaking.
     */
    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech()");
    }

    /**
     * What speech recognition error has occurred?
     *
     * @param errorCode
     * @return error Message
     */
    protected static String getErrorText(final int errorCode) {

        String message;

        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }
}