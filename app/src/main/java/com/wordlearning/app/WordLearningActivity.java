package com.wordlearning.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wordlearning.app.model.Word;
import com.wordlearning.app.service.SpeechRecognitionService;
import com.wordlearning.app.service.WordLearningService;

public class WordLearningActivity extends AppCompatActivity {
    private WordLearningService wordLearningService;
    private boolean isBound = false;
    private SpeechRecognitionService speechRecognitionService;
    
    private TextView wordTextView;
    private TextView pronunciationTextView;
    private TextView meaningTextView;
    private TextView exampleSentenceTextView;
    private TextView progressTextView;
    private Button playWordButton;
    private Button playSentenceButton;
    private Button previousButton;
    private Button nextButton;
    private Button markLearnedButton;
    private Button recordPronunciationButton;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            WordLearningService.LocalBinder binder = (WordLearningService.LocalBinder) service;
            wordLearningService = binder.getService();
            isBound = true;
            loadTodayWords();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_word_learning);
        
        initViews();
        bindService();
        initSpeechRecognition();
    }

    private void initViews() {
        wordTextView = findViewById(R.id.wordTextView);
        pronunciationTextView = findViewById(R.id.pronunciationTextView);
        meaningTextView = findViewById(R.id.meaningTextView);
        exampleSentenceTextView = findViewById(R.id.exampleSentenceTextView);
        progressTextView = findViewById(R.id.progressTextView);
        
        playWordButton = findViewById(R.id.playWordButton);
        playSentenceButton = findViewById(R.id.playSentenceButton);
        previousButton = findViewById(R.id.previousButton);
        nextButton = findViewById(R.id.nextButton);
        markLearnedButton = findViewById(R.id.markLearnedButton);
        recordPronunciationButton = findViewById(R.id.recordPronunciationButton);
        
        playWordButton.setOnClickListener(v -> playWord());
        playSentenceButton.setOnClickListener(v -> playSentence());
        previousButton.setOnClickListener(v -> showPreviousWord());
        nextButton.setOnClickListener(v -> showNextWord());
        markLearnedButton.setOnClickListener(v -> markWordAsLearned());
        recordPronunciationButton.setOnClickListener(v -> recordPronunciation());
    }

    private void bindService() {
        Intent intent = new Intent(this, WordLearningService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void initSpeechRecognition() {
        speechRecognitionService = new SpeechRecognitionService(this, new SpeechRecognitionService.SpeechRecognitionCallback() {
            @Override
            public void onSpeechRecognized(String text) {
                Toast.makeText(WordLearningActivity.this, "识别结果: " + text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSpeechError(String error) {
                Toast.makeText(WordLearningActivity.this, "语音识别错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadTodayWords() {
        if (wordLearningService != null) {
            wordLearningService.loadTodayWords();
            showCurrentWord();
        }
    }

    private void showCurrentWord() {
        Word word = wordLearningService.getCurrentWord();
        if (word != null) {
            wordTextView.setText(word.getWord());
            pronunciationTextView.setText(word.getPronunciation());
            meaningTextView.setText(word.getMeaning());
            exampleSentenceTextView.setText(word.getExampleSentence());
            
            int current = wordLearningService.getCurrentWordIndex() + 1;
            int total = wordLearningService.getTotalWordsCount();
            progressTextView.setText("进度: " + current + "/" + total);
        } else {
            finish();
            Toast.makeText(this, "今日学习完成！", Toast.LENGTH_SHORT).show();
        }
    }

    private void playWord() {
        Word word = wordLearningService.getCurrentWord();
        if (word != null) {
            wordLearningService.speakWord(word.getWord());
        }
    }

    private void playSentence() {
        Word word = wordLearningService.getCurrentWord();
        if (word != null && word.getExampleSentence() != null) {
            wordLearningService.speakSentence(word.getExampleSentence());
        }
    }

    private void showPreviousWord() {
        Word word = wordLearningService.getPreviousWord();
        if (word != null) {
            showCurrentWord();
        }
    }

    private void showNextWord() {
        Word word = wordLearningService.getNextWord();
        if (word != null) {
            showCurrentWord();
        }
    }

    private void markWordAsLearned() {
        Word word = wordLearningService.getCurrentWord();
        if (word != null) {
            wordLearningService.markWordAsLearned(word);
            showNextWord();
            Toast.makeText(this, "单词已标记为已学习", Toast.LENGTH_SHORT).show();
        }
    }

    private void recordPronunciation() {
        speechRecognitionService.startListening();
        Toast.makeText(this, "请开始发音...", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        if (speechRecognitionService != null) {
            speechRecognitionService.destroy();
        }
    }
}