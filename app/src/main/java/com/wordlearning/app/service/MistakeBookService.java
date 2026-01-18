package com.wordlearning.app.service;

import android.content.Context;
import android.util.Log;

import com.wordlearning.app.database.DatabaseHelper;
import com.wordlearning.app.model.MistakeWord;
import com.wordlearning.app.model.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MistakeBookService {
    private static final String TAG = "MistakeBookService";
    
    private DatabaseHelper dbHelper;
    private Context context;

    public MistakeBookService(Context context) {
        this.context = context;
        this.dbHelper = DatabaseHelper.getInstance(context);
    }

    public List<MistakeWord> getAllMistakeWords() {
        return dbHelper.getAllMistakeWords();
    }

    public Map<String, List<MistakeWord>> getMistakeWordsByWord() {
        List<MistakeWord> allMistakes = getAllMistakeWords();
        Map<String, List<MistakeWord>> mistakesByWord = new HashMap<>();
        
        for (MistakeWord mistake : allMistakes) {
            String word = mistake.getWord();
            if (!mistakesByWord.containsKey(word)) {
                mistakesByWord.put(word, new ArrayList<>());
            }
            mistakesByWord.get(word).add(mistake);
        }
        
        return mistakesByWord;
    }

    public List<MistakeWord> getMistakesByWord(String word) {
        List<MistakeWord> allMistakes = getAllMistakeWords();
        List<MistakeWord> wordMistakes = new ArrayList<>();
        
        for (MistakeWord mistake : allMistakes) {
            if (mistake.getWord().equalsIgnoreCase(word)) {
                wordMistakes.add(mistake);
            }
        }
        
        return wordMistakes;
    }

    public List<MistakeWord> getMistakesByType(String mistakeType) {
        List<MistakeWord> allMistakes = getAllMistakeWords();
        List<MistakeWord> typeMistakes = new ArrayList<>();
        
        for (MistakeWord mistake : allMistakes) {
            if (mistake.getMistakeType().equalsIgnoreCase(mistakeType)) {
                typeMistakes.add(mistake);
            }
        }
        
        return typeMistakes;
    }

    public void resolveMistakeWord(long wordId) {
        dbHelper.resolveMistakeWord(wordId);
        Log.d(TAG, "Resolved mistake word with ID: " + wordId);
    }

    public void resolveMistakeWordByWord(String word) {
        Word wordObj = getWordByString(word);
        if (wordObj != null) {
            resolveMistakeWord(wordObj.getId());
        }
    }

    public void resolveAllMistakesForWord(String word) {
        Word wordObj = getWordByString(word);
        if (wordObj != null) {
            resolveMistakeWord(wordObj.getId());
        }
    }

    public void clearAllMistakes() {
        List<MistakeWord> allMistakes = getAllMistakeWords();
        for (MistakeWord mistake : allMistakes) {
            resolveMistakeWord(mistake.getWordId());
        }
        Log.d(TAG, "Cleared all mistakes");
    }

    public int getMistakeCount() {
        return getAllMistakeWords().size();
    }

    public int getMistakeCountByWord(String word) {
        return getMistakesByWord(word).size();
    }

    public int getMistakeCountByType(String mistakeType) {
        return getMistakesByType(mistakeType).size();
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

    public Word getWordDetails(String word) {
        return getWordByString(word);
    }

    public void testWordAndResolve(String word, TestCallback callback) {
        Word wordObj = getWordDetails(word);
        if (wordObj == null) {
            callback.onError("Word not found");
            return;
        }

        List<MistakeWord> mistakes = getMistakesByWord(word);
        if (mistakes.isEmpty()) {
            callback.onTestResult(true, "No mistakes found for this word");
            return;
        }

        boolean allCorrect = true;
        StringBuilder result = new StringBuilder();

        for (MistakeWord mistake : mistakes) {
            String mistakeType = mistake.getMistakeType();
            result.append("Test ").append(mistakeType).append(": ");
            
            if (mistakeType.equalsIgnoreCase("pronunciation")) {
                callback.onPronunciationTestRequired(word);
            } else if (mistakeType.equalsIgnoreCase("meaning")) {
                callback.onMeaningTestRequired(word);
            } else if (mistakeType.equalsIgnoreCase("sentence")) {
                callback.onSentenceTestRequired(word);
            }
        }
    }

    public interface TestCallback {
        void onPronunciationTestRequired(String word);
        void onMeaningTestRequired(String word);
        void onSentenceTestRequired(String word);
        void onTestResult(boolean allCorrect, String result);
        void onError(String error);
    }
}