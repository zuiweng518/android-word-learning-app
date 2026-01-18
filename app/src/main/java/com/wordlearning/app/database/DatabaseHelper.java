package com.wordlearning.app.database;

import android.content.Context;
import android.os.Environment;

import com.wordlearning.app.model.MistakeWord;
import com.wordlearning.app.model.Word;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    private static final String DATABASE_NAME = "word_learning.db";
    private static final String DATABASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/WordLearning/";
    private static DatabaseHelper instance;
    private Connection connection;
    private Context context;

    private DatabaseHelper(Context context) {
        this.context = context.getApplicationContext();
        initializeDatabase();
    }

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context);
        }
        return instance;
    }

    private void initializeDatabase() {
        try {
            File dbDir = new File(DATABASE_PATH);
            if (!dbDir.exists()) {
                dbDir.mkdirs();
            }

            String url = "jdbc:h2:" + DATABASE_PATH + DATABASE_NAME;
            connection = DriverManager.getConnection(url, "sa", "");
            
            createTables();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() {
        try {
            Statement stmt = connection.createStatement();
            
            String createWordsTable = "CREATE TABLE IF NOT EXISTS words (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "word VARCHAR(255) NOT NULL, " +
                    "pronunciation TEXT, " +
                    "meaning TEXT, " +
                    "example_sentence TEXT, " +
                    "created_at BIGINT, " +
                    "last_review_time BIGINT, " +
                    "review_count INT DEFAULT 0, " +
                    "is_learned BOOLEAN DEFAULT FALSE)";
            
            String createMistakeWordsTable = "CREATE TABLE IF NOT EXISTS mistake_words (" +
                    "id IDENTITY PRIMARY KEY, " +
                    "word_id BIGINT, " +
                    "word VARCHAR(255) NOT NULL, " +
                    "mistake_type VARCHAR(50), " +
                    "added_at BIGINT, " +
                    "mistake_count INT DEFAULT 1, " +
                    "is_resolved BOOLEAN DEFAULT FALSE)";
            
            stmt.execute(createWordsTable);
            stmt.execute(createMistakeWordsTable);
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public long insertWord(Word word) {
        try {
            String sql = "INSERT INTO words (word, pronunciation, meaning, example_sentence, created_at, last_review_time, review_count, is_learned) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, word.getWord());
            stmt.setString(2, word.getPronunciation());
            stmt.setString(3, word.getMeaning());
            stmt.setString(4, word.getExampleSentence());
            stmt.setLong(5, word.getCreatedAt());
            stmt.setLong(6, word.getLastReviewTime());
            stmt.setInt(7, word.getReviewCount());
            stmt.setBoolean(8, word.isLearned());
            
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                long id = rs.getLong(1);
                stmt.close();
                return id;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void updateWord(Word word) {
        try {
            String sql = "UPDATE words SET pronunciation = ?, meaning = ?, example_sentence = ?, " +
                    "last_review_time = ?, review_count = ?, is_learned = ? WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setString(1, word.getPronunciation());
            stmt.setString(2, word.getMeaning());
            stmt.setString(3, word.getExampleSentence());
            stmt.setLong(4, word.getLastReviewTime());
            stmt.setInt(5, word.getReviewCount());
            stmt.setBoolean(6, word.isLearned());
            stmt.setLong(7, word.getId());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Word getWordById(long id) {
        try {
            String sql = "SELECT * FROM words WHERE id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Word word = new Word();
                word.setId(rs.getLong("id"));
                word.setWord(rs.getString("word"));
                word.setPronunciation(rs.getString("pronunciation"));
                word.setMeaning(rs.getString("meaning"));
                word.setExampleSentence(rs.getString("example_sentence"));
                word.setCreatedAt(rs.getLong("created_at"));
                word.setLastReviewTime(rs.getLong("last_review_time"));
                word.setReviewCount(rs.getInt("review_count"));
                word.setLearned(rs.getBoolean("is_learned"));
                stmt.close();
                return word;
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Word> getAllWords() {
        List<Word> words = new ArrayList<>();
        try {
            String sql = "SELECT * FROM words ORDER BY created_at DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                Word word = new Word();
                word.setId(rs.getLong("id"));
                word.setWord(rs.getString("word"));
                word.setPronunciation(rs.getString("pronunciation"));
                word.setMeaning(rs.getString("meaning"));
                word.setExampleSentence(rs.getString("example_sentence"));
                word.setCreatedAt(rs.getLong("created_at"));
                word.setLastReviewTime(rs.getLong("last_review_time"));
                word.setReviewCount(rs.getInt("review_count"));
                word.setLearned(rs.getBoolean("is_learned"));
                words.add(word);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return words;
    }

    public List<Word> getRandomWords(int count) {
        List<Word> words = new ArrayList<>();
        try {
            String sql = "SELECT * FROM words ORDER BY RANDOM() LIMIT ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setInt(1, count);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Word word = new Word();
                word.setId(rs.getLong("id"));
                word.setWord(rs.getString("word"));
                word.setPronunciation(rs.getString("pronunciation"));
                word.setMeaning(rs.getString("meaning"));
                word.setExampleSentence(rs.getString("example_sentence"));
                word.setCreatedAt(rs.getLong("created_at"));
                word.setLastReviewTime(rs.getLong("last_review_time"));
                word.setReviewCount(rs.getInt("review_count"));
                word.setLearned(rs.getBoolean("is_learned"));
                words.add(word);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return words;
    }

    public List<Word> getWordsForReview(long days) {
        List<Word> words = new ArrayList<>();
        try {
            long timeThreshold = System.currentTimeMillis() - (days * 24 * 60 * 60 * 1000);
            String sql = "SELECT * FROM words WHERE last_review_time < ? OR last_review_time = 0 ORDER BY last_review_time ASC LIMIT 10";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, timeThreshold);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Word word = new Word();
                word.setId(rs.getLong("id"));
                word.setWord(rs.getString("word"));
                word.setPronunciation(rs.getString("pronunciation"));
                word.setMeaning(rs.getString("meaning"));
                word.setExampleSentence(rs.getString("example_sentence"));
                word.setCreatedAt(rs.getLong("created_at"));
                word.setLastReviewTime(rs.getLong("last_review_time"));
                word.setReviewCount(rs.getInt("review_count"));
                word.setLearned(rs.getBoolean("is_learned"));
                words.add(word);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return words;
    }

    public void insertMistakeWord(MistakeWord mistakeWord) {
        try {
            String sql = "INSERT INTO mistake_words (word_id, word, mistake_type, added_at, mistake_count, is_resolved) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, mistakeWord.getWordId());
            stmt.setString(2, mistakeWord.getWord());
            stmt.setString(3, mistakeWord.getMistakeType());
            stmt.setLong(4, mistakeWord.getAddedAt());
            stmt.setInt(5, mistakeWord.getMistakeCount());
            stmt.setBoolean(6, mistakeWord.isResolved());
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<MistakeWord> getAllMistakeWords() {
        List<MistakeWord> mistakeWords = new ArrayList<>();
        try {
            String sql = "SELECT * FROM mistake_words WHERE is_resolved = FALSE ORDER BY added_at DESC";
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            
            while (rs.next()) {
                MistakeWord mistakeWord = new MistakeWord();
                mistakeWord.setId(rs.getLong("id"));
                mistakeWord.setWordId(rs.getLong("word_id"));
                mistakeWord.setWord(rs.getString("word"));
                mistakeWord.setMistakeType(rs.getString("mistake_type"));
                mistakeWord.setAddedAt(rs.getLong("added_at"));
                mistakeWord.setMistakeCount(rs.getInt("mistake_count"));
                mistakeWord.setResolved(rs.getBoolean("is_resolved"));
                mistakeWords.add(mistakeWord);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mistakeWords;
    }

    public void resolveMistakeWord(long wordId) {
        try {
            String sql = "UPDATE mistake_words SET is_resolved = TRUE WHERE word_id = ?";
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setLong(1, wordId);
            stmt.executeUpdate();
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}