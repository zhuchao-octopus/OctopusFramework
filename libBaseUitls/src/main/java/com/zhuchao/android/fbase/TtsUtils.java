package com.zhuchao.android.fbase;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;

import java.util.Locale;

public class TtsUtils {

    private static final String TAG = "TtsUtils";
    private TextToSpeech textToSpeech;
    private final Context context;

    public TtsUtils(Context context) {
        this.context = context;
        initTextToSpeech();
    }

    private void initTextToSpeech() {
        textToSpeech = new TextToSpeech(context, new OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.US);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        MMLog.d(TAG, "Language is not supported or missing data");
                    } else {
                        MMLog.d(TAG, "TTS Initialization successful");
                    }
                } else {
                    MMLog.d(TAG, "TTS Initialization failed");
                }
            }
        });
    }

    public void speak(String text) {
        if (textToSpeech != null) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    public void stop() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
    }

    public void shutdown() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    public void setLanguage(Locale locale) {
        if (textToSpeech != null) {
            int result = textToSpeech.setLanguage(locale);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                MMLog.d(TAG, "Language is not supported or missing data");
            } else {
                MMLog.d(TAG, "Language set to: " + locale.toString());
            }
        }
    }

    public void setPitch(float pitch) {
        if (textToSpeech != null) {
            textToSpeech.setPitch(pitch);
        }
    }

    public void setSpeechRate(float speechRate) {
        if (textToSpeech != null) {
            textToSpeech.setSpeechRate(speechRate);
        }
    }

}
