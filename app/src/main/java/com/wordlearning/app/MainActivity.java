package com.wordlearning.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.wordlearning.app.database.DatabaseHelper;
import com.wordlearning.app.model.Word;
import com.wordlearning.app.service.EmailService;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private static final int REQUEST_STORAGE_PERMISSION = 2;
    private static final int REQUEST_EMAIL_PERMISSION = 3;
    
    private DatabaseHelper dbHelper;
    private TextView wordCountTextView;
    private TextView mistakeCountTextView;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        dbHelper = DatabaseHelper.getInstance(this);
        
        initViews();
        checkPermissions();
        updateWordCount();
    }
    
    private void initViews() {
        wordCountTextView = findViewById(R.id.wordCountTextView);
        mistakeCountTextView = findViewById(R.id.mistakeCountTextView);
        
        Button startLearningButton = findViewById(R.id.startLearningButton);
        Button importWordsButton = findViewById(R.id.importWordsButton);
        Button mistakeBookButton = findViewById(R.id.mistakeBookButton);
        Button reviewButton = findViewById(R.id.reviewButton);
        Button settingsButton = findViewById(R.id.settingsButton);
        
        startLearningButton.setOnClickListener(v -> startLearning());
        importWordsButton.setOnClickListener(v -> importWords());
        mistakeBookButton.setOnClickListener(v -> openMistakeBook());
        reviewButton.setOnClickListener(v -> startReview());
        settingsButton.setOnClickListener(v -> openSettings());
    }
    
    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECORD_AUDIO},
                    REQUEST_RECORD_AUDIO_PERMISSION);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) 
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQUEST_STORAGE_PERMISSION);
        }
    }
    
    private void startLearning() {
        Intent intent = new Intent(this, WordLearningActivity.class);
        startActivity(intent);
    }
    
    private void importWords() {
        Intent intent = new Intent(this, MistakeBookActivity.class);
        startActivity(intent);
    }
    
    private void openMistakeBook() {
        Intent intent = new Intent(this, MistakeBookActivity.class);
        startActivity(intent);
    }
    
    private void startReview() {
        Intent intent = new Intent(this, ReviewActivity.class);
        startActivity(intent);
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
    
    private void updateWordCount() {
        List<Word> words = dbHelper.getAllWords();
        wordCountTextView.setText("单词总数: " + words.size());
        
        int mistakeCount = dbHelper.getAllMistakeWords().size();
        mistakeCountTextView.setText("错词数量: " + mistakeCount);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        updateWordCount();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "录音权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要录音权限才能使用语音功能", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_STORAGE_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "存储权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "需要存储权限才能使用应用", Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_EMAIL_PERMISSION && resultCode == RESULT_OK) {
            updateWordCount();
            Toast.makeText(this, "单词导入成功", Toast.LENGTH_SHORT).show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}