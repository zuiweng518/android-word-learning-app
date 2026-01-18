package com.wordlearning.app;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wordlearning.app.model.Word;
import com.wordlearning.app.service.ReviewService;
import com.wordlearning.app.service.SpeechRecognitionService;

public class ReviewActivity extends AppCompatActivity {
    private ReviewService reviewService;
    private SpeechRecognitionService speechRecognitionService;
    
    private TextView wordTextView;
    private TextView pronunciationTextView;
    private TextView meaningTextView;
    private TextView exampleSentenceTextView;
    private TextView progressTextView;
    private EditText pronunciationInput;
    private EditText meaningInput;
    private EditText sentenceInput;
    private Button playWordButton;
    private Button playSentenceButton;
    private Button recordPronunciationButton;
    private Button checkPronunciationButton;
    private Button checkMeaningButton;
    private Button checkSentenceButton;
    private Button nextButton;
    private Button finishReviewButton;
    
    private Word currentWord;
    private int currentReviewIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        
        reviewService = new ReviewService(this);
        
        initViews();
        initSpeechRecognition();
        checkAndStartReview();
    }

    private void initViews() {
        wordTextView = findViewById(R.id.reviewWordTextView);
        pronunciationTextView = findViewById(R.id.reviewPronunciationTextView);
        meaningTextView = findViewById(R.id.reviewMeaningTextView);
        exampleSentenceTextView = findViewById(R.id.reviewExampleSentenceTextView);
        progressTextView = findViewById(R.id.reviewProgressTextView);
        
        pronunciationInput = findViewById(R.id.pronunciationInput);
        meaningInput = findViewById(R.id.meaningInput);
        sentenceInput = findViewById(R.id.sentenceInput);
        
        playWordButton = findViewById(R.id.playWordButton);
        playSentenceButton = findViewById(R.id.playSentenceButton);
        recordPronunciationButton = findViewById(R.id.recordPronunciationButton);
        checkPronunciationButton = findViewById(R.id.checkPronunciationButton);
        checkMeaningButton = findViewById(R.id.checkMeaningButton);
        checkSentenceButton = findViewById(R.id.checkSentenceButton);
        nextButton = findViewById(R.id.nextReviewButton);
        finishReviewButton = findViewById(R.id.finishReviewButton);
        
        playWordButton.setOnClickListener(v -> playWord());
        playSentenceButton.setOnClickListener(v -> playSentence());
        recordPronunciationButton.setOnClickListener(v -> recordPronunciation());
        checkPronunciationButton.setOnClickListener(v -> checkPronunciation());
        checkMeaningButton.setOnClickListener(v -> checkMeaning());
        checkSentenceButton.setOnClickListener(v -> checkSentence());
        nextButton.setOnClickListener(v -> nextWord());
        finishReviewButton.setOnClickListener(v -> finishReview());
    }

    private void initSpeechRecognition() {
        speechRecognitionService = new SpeechRecognitionService(this, new SpeechRecognitionService.SpeechRecognitionCallback() {
            @Override
            public void onSpeechRecognized(String text) {
                pronunciationInput.setText(text);
            }

            @Override
            public void onSpeechError(String error) {
                Toast.makeText(ReviewActivity.this, "语音识别错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkAndStartReview() {
        if (reviewService.shouldStartReview()) {
            reviewService.loadReviewWords();
            showCurrentReviewWord();
        } else {
            Toast.makeText(this, "还没有到复习时间", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void showCurrentReviewWord() {
        currentWord = reviewService.getCurrentReviewWord();
        if (currentWord != null) {
            wordTextView.setText(currentWord.getWord());
            pronunciationTextView.setText(currentWord.getPronunciation());
            meaningTextView.setText(currentWord.getMeaning());
            exampleSentenceTextView.setText(currentWord.getExampleSentence());
            
            int current = reviewService.getCurrentReviewIndex() + 1;
            int total = reviewService.getTotalReviewWordsCount();
            progressTextView.setText("复习进度: " + current + "/" + total);
            
            clearInputs();
        } else {
            finish();
            Toast.makeText(this, "复习完成！", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearInputs() {
        pronunciationInput.setText("");
        meaningInput.setText("");
        sentenceInput.setText("");
    }

    private void playWord() {
        if (currentWord != null) {
            Intent intent = new Intent(this, WordLearningService.class);
            intent.setAction("PLAY_WORD");
            intent.putExtra("word", currentWord.getWord());
            startService(intent);
        }
    }

    private void playSentence() {
        if (currentWord != null && currentWord.getExampleSentence() != null) {
            Intent intent = new Intent(this, WordLearningService.class);
            intent.setAction("PLAY_SENTENCE");
            intent.putExtra("sentence", currentWord.getExampleSentence());
            startService(intent);
        }
    }

    private void recordPronunciation() {
        speechRecognitionService.startListening();
        Toast.makeText(this, "请开始发音...", Toast.LENGTH_SHORT).show();
    }

    private void checkPronunciation() {
        String userPronunciation = pronunciationInput.getText().toString().trim();
        if (userPronunciation.isEmpty()) {
            Toast.makeText(this, "请输入发音", Toast.LENGTH_SHORT).show();
            return;
        }
        
        reviewService.checkPronunciation(currentWord.getWord(), userPronunciation, new ReviewService.PronunciationCheckCallback() {
            @Override
            public void onPronunciationChecked(boolean isCorrect) {
                if (isCorrect) {
                    Toast.makeText(ReviewActivity.this, "发音正确！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReviewActivity.this, "发音错误，已添加到错词本", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ReviewActivity.this, "检查发音错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkMeaning() {
        String userMeaning = meaningInput.getText().toString().trim();
        if (userMeaning.isEmpty()) {
            Toast.makeText(this, "请输入意思", Toast.LENGTH_SHORT).show();
            return;
        }
        
        reviewService.checkMeaning(currentWord.getWord(), userMeaning, new ReviewService.MeaningCheckCallback() {
            @Override
            public void onMeaningChecked(boolean isCorrect) {
                if (isCorrect) {
                    Toast.makeText(ReviewActivity.this, "意思正确！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReviewActivity.this, "意思错误，已添加到错词本", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ReviewActivity.this, "检查意思错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void checkSentence() {
        String userSentence = sentenceInput.getText().toString().trim();
        if (userSentence.isEmpty()) {
            Toast.makeText(this, "请输入句子", Toast.LENGTH_SHORT).show();
            return;
        }
        
        reviewService.checkSentence(currentWord.getWord(), userSentence, new ReviewService.SentenceCheckCallback() {
            @Override
            public void onSentenceChecked(boolean isCorrect) {
                if (isCorrect) {
                    Toast.makeText(ReviewActivity.this, "造句正确！", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ReviewActivity.this, "造句错误，已添加到错词本", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(ReviewActivity.this, "检查造句错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void nextWord() {
        reviewService.getNextReviewWord();
        showCurrentReviewWord();
    }

    private void finishReview() {
        reviewService.markReviewCompleted();
        finish();
        Toast.makeText(this, "复习已完成，下次复习时间：3天后", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (speechRecognitionService != null) {
            speechRecognitionService.destroy();
        }
    }
}