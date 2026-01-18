package com.wordlearning.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.wordlearning.app.database.DatabaseHelper;
import com.wordlearning.app.model.Word;

import java.util.List;
import java.util.Locale;

public class WordLearningService extends Service implements TextToSpeech.OnInitListener {
    private static final String TAG = "WordLearningService";
    private static final int WORDS_PER_DAY = 10;
    
    private final IBinder binder = new LocalBinder();
    private TextToSpeech tts;
    private DatabaseHelper dbHelper;
    private List<Word> todayWords;
    private int currentWordIndex = 0;
    private boolean isTtsInitialized = false;

    public class LocalBinder extends Binder {
        WordLearningService getService() {
            return WordLearningService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        dbHelper = DatabaseHelper.getInstance(this);
        tts = new TextToSpeech(this, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e(TAG, "Language not supported");
            } else {
                isTtsInitialized = true;
                Log.d(TAG, "TTS initialized successfully");
            }
        } else {
            Log.e(TAG, "TTS initialization failed");
        }
    }

    public void loadTodayWords() {
        todayWords = dbHelper.getRandomWords(WORDS_PER_DAY);
        currentWordIndex = 0;
        Log.d(TAG, "Loaded " + todayWords.size() + " words for today");
    }

    public Word getCurrentWord() {
        if (todayWords == null || currentWordIndex >= todayWords.size()) {
            return null;
        }
        return todayWords.get(currentWordIndex);
    }

    public Word getNextWord() {
        if (todayWords == null || currentWordIndex >= todayWords.size() - 1) {
            return null;
        }
        currentWordIndex++;
        return todayWords.get(currentWordIndex);
    }

    public Word getPreviousWord() {
        if (todayWords == null || currentWordIndex <= 0) {
            return null;
        }
        currentWordIndex--;
        return todayWords.get(currentWordIndex);
    }

    public int getCurrentWordIndex() {
        return currentWordIndex;
    }

    public int getTotalWordsCount() {
        return todayWords != null ? todayWords.size() : 0;
    }

    public void speakWord(String word) {
        if (isTtsInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(word, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void speakSentence(String sentence) {
        if (isTtsInitialized) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, null);
            } else {
                tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    public void stopSpeaking() {
        if (tts != null) {
            tts.stop();
        }
    }

    public void setSpeechRate(float rate) {
        if (tts != null) {
            tts.setSpeechRate(rate);
        }
    }

    public void setPitch(float pitch) {
        if (tts != null) {
            tts.setPitch(pitch);
        }
    }

    public boolean isTtsInitialized() {
        return isTtsInitialized;
    }

    public void markWordAsLearned(Word word) {
        word.setLearned(true);
        word.setLastReviewTime(System.currentTimeMillis());
        word.setReviewCount(word.getReviewCount() + 1);
        dbHelper.updateWord(word);
    }

    public void resetTodayProgress() {
        currentWordIndex = 0;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}