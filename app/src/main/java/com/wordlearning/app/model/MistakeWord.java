package com.wordlearning.app.model;

public class MistakeWord {
    private long id;
    private long wordId;
    private String word;
    private String mistakeType;
    private long addedAt;
    private int mistakeCount;
    private boolean isResolved;

    public MistakeWord() {
    }

    public MistakeWord(long wordId, String word, String mistakeType) {
        this.wordId = wordId;
        this.word = word;
        this.mistakeType = mistakeType;
        this.addedAt = System.currentTimeMillis();
        this.mistakeCount = 1;
        this.isResolved = false;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getWordId() {
        return wordId;
    }

    public void setWordId(long wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMistakeType() {
        return mistakeType;
    }

    public void setMistakeType(String mistakeType) {
        this.mistakeType = mistakeType;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }

    public int getMistakeCount() {
        return mistakeCount;
    }

    public void setMistakeCount(int mistakeCount) {
        this.mistakeCount = mistakeCount;
    }

    public boolean isResolved() {
        return isResolved;
    }

    public void setResolved(boolean resolved) {
        isResolved = resolved;
    }
}