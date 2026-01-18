package com.wordlearning.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wordlearning.app.database.DatabaseHelper;
import com.wordlearning.app.model.Word;
import com.wordlearning.app.service.GmailService;

import java.util.List;

public class GmailImportActivity extends AppCompatActivity {
    private GmailService gmailService;
    private DatabaseHelper dbHelper;
    
    private TextView statusTextView;
    private ProgressBar progressBar;
    private Button importButton;
    private Button cancelButton;
    
    private List<String> importedWords;
    private int importedCount = 0;
    private int skippedCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gmail_import);
        
        dbHelper = DatabaseHelper.getInstance(this);
        
        initViews();
        initGmailService();
    }

    private void initViews() {
        statusTextView = findViewById(R.id.statusTextView);
        progressBar = findViewById(R.id.progressBar);
        importButton = findViewById(R.id.importButton);
        cancelButton = findViewById(R.id.cancelButton);
        
        importButton.setOnClickListener(v -> startImport());
        cancelButton.setOnClickListener(v -> finish());
        
        progressBar.setVisibility(View.GONE);
        statusTextView.setText("准备从Gmail导入单词...");
    }

    private void initGmailService() {
        gmailService = new GmailService(this, new GmailService.WordImportCallback() {
            @Override
            public void onWordsImported(List<String> words) {
                importedWords = words;
                processImportedWords();
            }

            @Override
            public void onError(String error) {
                statusTextView.setText("导入失败: " + error);
                progressBar.setVisibility(View.GONE);
                importButton.setEnabled(true);
                Toast.makeText(GmailImportActivity.this, "导入失败: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startImport() {
        importButton.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        statusTextView.setText("正在从Gmail读取邮件...");
        
        gmailService.importWordsFromGmail();
    }

    private void processImportedWords() {
        if (importedWords == null || importedWords.isEmpty()) {
            statusTextView.setText("没有找到需要导入的单词");
            progressBar.setVisibility(View.GONE);
            importButton.setEnabled(true);
            Toast.makeText(this, "没有找到需要导入的单词", Toast.LENGTH_SHORT).show();
            return;
        }
        
        statusTextView.setText("找到 " + importedWords.size() + " 个单词，正在导入...");
        
        new Thread(() -> {
            importedCount = 0;
            skippedCount = 0;
            
            for (String word : importedWords) {
                Word existingWord = getWordByString(word);
                if (existingWord == null) {
                    Word newWord = new Word(word);
                    dbHelper.insertWord(newWord);
                    importedCount++;
                } else {
                    skippedCount++;
                }
            }
            
            runOnUiThread(() -> {
                String result = String.format("导入完成！\n成功导入: %d 个单词\n跳过: %d 个重复单词", 
                        importedCount, skippedCount);
                statusTextView.setText(result);
                progressBar.setVisibility(View.GONE);
                
                Intent resultIntent = new Intent();
                resultIntent.putExtra("imported_count", importedCount);
                setResult(RESULT_OK, resultIntent);
                
                Toast.makeText(this, result, Toast.LENGTH_LONG).show();
                
                importButton.setEnabled(true);
            });
        }).start();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            gmailService.importWordsFromGmail();
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