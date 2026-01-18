package com.wordlearning.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wordlearning.app.model.MistakeWord;
import com.wordlearning.app.model.Word;
import com.wordlearning.app.service.MistakeBookService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MistakeBookActivity extends AppCompatActivity {
    private MistakeBookService mistakeBookService;
    private ListView mistakeListView;
    private TextView mistakeCountTextView;
    private Button clearAllButton;
    private Button testWordButton;
    private Button resolveButton;
    
    private List<MistakeWord> mistakeWords;
    private MistakeWord selectedMistakeWord;
    private MistakeAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mistake_book);
        
        mistakeBookService = new MistakeBookService(this);
        
        initViews();
        loadMistakeWords();
    }

    private void initViews() {
        mistakeListView = findViewById(R.id.mistakeListView);
        mistakeCountTextView = findViewById(R.id.mistakeCountTextView);
        clearAllButton = findViewById(R.id.clearAllButton);
        testWordButton = findViewById(R.id.testWordButton);
        resolveButton = findViewById(R.id.resolveButton);
        
        mistakeListView.setOnItemClickListener((parent, view, position, id) -> {
            selectedMistakeWord = mistakeWords.get(position);
            testWordButton.setEnabled(true);
            resolveButton.setEnabled(true);
        });
        
        clearAllButton.setOnClickListener(v -> clearAllMistakes());
        testWordButton.setOnClickListener(v -> testSelectedWord());
        resolveButton.setOnClickListener(v -> resolveSelectedWord());
        
        testWordButton.setEnabled(false);
        resolveButton.setEnabled(false);
    }

    private void loadMistakeWords() {
        mistakeWords = mistakeBookService.getAllMistakeWords();
        mistakeCountTextView.setText("错词数量: " + mistakeWords.size());
        
        adapter = new MistakeAdapter(mistakeWords);
        mistakeListView.setAdapter(adapter);
    }

    private void clearAllMistakes() {
        if (mistakeWords.isEmpty()) {
            Toast.makeText(this, "错词本为空", Toast.LENGTH_SHORT).show();
            return;
        }
        
        mistakeBookService.clearAllMistakes();
        loadMistakeWords();
        Toast.makeText(this, "已清空错词本", Toast.LENGTH_SHORT).show();
    }

    private void testSelectedWord() {
        if (selectedMistakeWord == null) {
            Toast.makeText(this, "请先选择一个单词", Toast.LENGTH_SHORT).show();
            return;
        }
        
        mistakeBookService.testWordAndResolve(selectedMistakeWord.getWord(), new MistakeBookService.TestCallback() {
            @Override
            public void onPronunciationTestRequired(String word) {
                Toast.makeText(MistakeBookActivity.this, "需要测试发音", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onMeaningTestRequired(String word) {
                Toast.makeText(MistakeBookActivity.this, "需要测试意思", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSentenceTestRequired(String word) {
                Toast.makeText(MistakeBookActivity.this, "需要测试造句", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onTestResult(boolean allCorrect, String result) {
                if (allCorrect) {
                    mistakeBookService.resolveMistakeWordByWord(selectedMistakeWord.getWord());
                    loadMistakeWords();
                    Toast.makeText(MistakeBookActivity.this, "测试通过，单词已从错词本移除", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MistakeBookActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MistakeBookActivity.this, "测试错误: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void resolveSelectedWord() {
        if (selectedMistakeWord == null) {
            Toast.makeText(this, "请先选择一个单词", Toast.LENGTH_SHORT).show();
            return;
        }
        
        mistakeBookService.resolveMistakeWordByWord(selectedMistakeWord.getWord());
        loadMistakeWords();
        Toast.makeText(this, "单词已从错词本移除", Toast.LENGTH_SHORT).show();
        
        selectedMistakeWord = null;
        testWordButton.setEnabled(false);
        resolveButton.setEnabled(false);
    }

    private class MistakeAdapter extends BaseAdapter {
        private List<MistakeWord> mistakes;

        public MistakeAdapter(List<MistakeWord> mistakes) {
            this.mistakes = mistakes;
        }

        @Override
        public int getCount() {
            return mistakes.size();
        }

        @Override
        public Object getItem(int position) {
            return mistakes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(MistakeBookActivity.this)
                        .inflate(R.layout.item_mistake, parent, false);
            }
            
            MistakeWord mistake = mistakes.get(position);
            
            TextView wordTextView = convertView.findViewById(R.id.mistakeWordTextView);
            TextView typeTextView = convertView.findViewById(R.id.mistakeTypeTextView);
            TextView countTextView = convertView.findViewById(R.id.mistakeCountTextView);
            
            wordTextView.setText(mistake.getWord());
            typeTextView.setText("错误类型: " + mistake.getMistakeType());
            countTextView.setText("错误次数: " + mistake.getMistakeCount());
            
            return convertView;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMistakeWords();
    }
}