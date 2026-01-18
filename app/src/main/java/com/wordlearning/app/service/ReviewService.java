package com.wordlearning.app.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.wordlearning.app.database.DatabaseHelper;
import com.wordlearning.app.model.MistakeWord;
import com.wordlearning.app.model.Word;

import java.util.ArrayList;
import java.util.List;

public class ReviewService {
    private static final String TAG = "ReviewService";
    private static final String PREFS_NAME = "ReviewPrefs";
    private static final String LAST_REVIEW_TIME = "last_review_time";
    private static final long REVIEW_INTERVAL = 3 * 24 * 60 * 60 * 1000;
    
    private Context context;
    private DatabaseHelper dbHelper;
    private ZhipuAIService aiService;
    private List<Word> reviewWords;
    private int currentReviewIndex = 0;
    private SharedPreferences prefs;

    public ReviewService(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
        this.aiService = new ZhipuAIService(context);
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean shouldStartReview() {
        long lastReviewTime = prefs.getLong(LAST_REVIEW_TIME, 0);
        long currentTime = System.currentTimeMillis();
        return (currentTime - lastReviewTime) >= REVIEW_INTERVAL;
    }

    public void loadReviewWords() {
        reviewWords = dbHelper.getWordsForReview(3);
        currentReviewIndex = 0;
        Log.d(TAG, "Loaded " + reviewWords.size() + " words for review");
    }

    public Word getCurrentReviewWord() {
        if (reviewWords == null || currentReviewIndex >= reviewWords.size()) {
            return null;
        }
        return reviewWords.get(currentReviewIndex);
    }

    public Word getNextReviewWord() {
        if (reviewWords == null || currentReviewIndex >= reviewWords.size() - 1) {
            return null;
        }
        currentReviewIndex++;
        return reviewWords.get(currentReviewIndex);
    }

    public int getCurrentReviewIndex() {
        return currentReviewIndex;
    }

    public int getTotalReviewWordsCount() {
        return reviewWords != null ? reviewWords.size() : 0;
    }

    public void checkPronunciation(String word, String userPronunciation, PronunciationCheckCallback callback) {
        aiService.checkPronunciation(word, userPronunciation, new ZhipuAIService.PronunciationCheckCallback() {
            @Override
            public void onPronunciationChecked(boolean isCorrect) {
                if (!isCorrect) {
                    addMistakeWord(word, "pronunciation");
                }
                callback.onPronunciationChecked(isCorrect);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void checkMeaning(String word, String userMeaning, MeaningCheckCallback callback) {
        aiService.checkMeaning(word, userMeaning, new ZhipuAIService.MeaningCheckCallback() {
            @Override
            public void onMeaningChecked(boolean isCorrect) {
                if (!isCorrect) {
                    addMistakeWord(word, "meaning");
                }
                callback.onMeaningChecked(isCorrect);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void checkSentence(String word, String userSentence, SentenceCheckCallback callback) {
        aiService.checkSentence(word, userSentence, new ZhipuAIService.SentenceCheckCallback() {
            @Override
            public void onSentenceChecked(boolean isCorrect) {
                if (!isCorrect) {
                    addMistakeWord(word, "sentence");
                }
                callback.onSentenceChecked(isCorrect);
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    private void addMistakeWord(String word, String mistakeType) {
        Word wordObj = getWordByString(word);
        if (wordObj != null) {
            MistakeWord mistakeWord = new MistakeWord(wordObj.getId(), word, mistakeType);
            dbHelper.insertMistakeWord(mistakeWord);
            Log.d(TAG, "Added mistake word: " + word + " (" + mistakeType + ")");
        }
    }

    private Word getWordByString(String word) {
        List<Word> allWords = dbHelper.getAllWords();
        for (Word w : allWords) {
            if (w.getWord().equalsIgnoreCase(word)) {
                return w;
            }
        }
        return null;
    }

    public void markReviewCompleted() {
        prefs.edit().putLong(LAST_REVIEW_TIME, System.currentTimeMillis()).apply();
        Log.d(TAG, "Review completed");
    }

    public interface PronunciationCheckCallback {
        void onPronunciationChecked(boolean isCorrect);
        void onError(String error);
    }

    public interface MeaningCheckCallback {
        void onMeaningChecked(boolean isCorrect);
        void onError(String error);
    }

    public interface SentenceCheckCallback {
        void onSentenceChecked(boolean isCorrect);
        void onError(String error);
    }
}