package com.wordlearning.app.service;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wordlearning.app.model.Word;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ZhipuAIService {
    private static final String TAG = "ZhipuAIService";
    private static final String API_URL = "https://open.bigmodel.cn/api/paas/v4/chat/completions";
    
    private OkHttpClient client;
    private Gson gson;
    private Context context;

    public ZhipuAIService(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
    }

    public void getWordInfo(String word, WordInfoCallback callback) {
        new GetWordInfoTask(word, callback).execute();
    }

    public void checkPronunciation(String word, String pronunciation, PronunciationCheckCallback callback) {
        new CheckPronunciationTask(word, pronunciation, callback).execute();
    }

    public void checkMeaning(String word, String userMeaning, MeaningCheckCallback callback) {
        new CheckMeaningTask(word, userMeaning, callback).execute();
    }

    public void checkSentence(String word, String userSentence, SentenceCheckCallback callback) {
        new CheckSentenceTask(word, userSentence, callback).execute();
    }

    private class GetWordInfoTask extends AsyncTask<Void, Void, Word> {
        private String word;
        private WordInfoCallback callback;
        private String error = null;

        public GetWordInfoTask(String word, WordInfoCallback callback) {
            this.word = word;
            this.callback = callback;
        }

        @Override
        protected Word doInBackground(Void... voids) {
            try {
                String prompt = String.format(
                        "请为单词\"%s\"提供以下信息，以JSON格式返回：\n" +
                        "{\n" +
                        "  \"pronunciation\": \"音标\",\n" +
                        "  \"meaning\": \"中文意思\",\n" +
                        "  \"example_sentence\": \"英文例句\"\n" +
                        "}",
                        word
                );

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", "glm-4");
                requestBody.add("messages", gson.toJsonTree(new Object[]{
                        new Message("user", prompt)
                }));

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + getApiKey())
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String content = jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                JsonObject wordInfo = gson.fromJson(content, JsonObject.class);

                Word wordObj = new Word(word);
                wordObj.setPronunciation(wordInfo.get("pronunciation").getAsString());
                wordObj.setMeaning(wordInfo.get("meaning").getAsString());
                wordObj.setExampleSentence(wordInfo.get("example_sentence").getAsString());

                return wordObj;
            } catch (IOException e) {
                error = "Error getting word info: " + e.getMessage();
                Log.e(TAG, error, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(Word word) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onWordInfoReceived(word);
            }
        }
    }

    private class CheckPronunciationTask extends AsyncTask<Void, Void, Boolean> {
        private String word;
        private String pronunciation;
        private PronunciationCheckCallback callback;
        private String error = null;

        public CheckPronunciationTask(String word, String pronunciation, PronunciationCheckCallback callback) {
            this.word = word;
            this.pronunciation = pronunciation;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String prompt = String.format(
                        "请判断用户对单词\"%s\"的发音\"%s\"是否准确。请只回答\"正确\"或\"错误\"。",
                        word, pronunciation
                );

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", "glm-4");
                requestBody.add("messages", gson.toJsonTree(new Object[]{
                        new Message("user", prompt)
                }));

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + getApiKey())
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String content = jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                return content.contains("正确");
            } catch (IOException e) {
                error = "Error checking pronunciation: " + e.getMessage();
                Log.e(TAG, error, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isCorrect) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onPronunciationChecked(isCorrect);
            }
        }
    }

    private class CheckMeaningTask extends AsyncTask<Void, Void, Boolean> {
        private String word;
        private String userMeaning;
        private MeaningCheckCallback callback;
        private String error = null;

        public CheckMeaningTask(String word, String userMeaning, MeaningCheckCallback callback) {
            this.word = word;
            this.userMeaning = userMeaning;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String prompt = String.format(
                        "请判断用户对单词\"%s\"的意思理解\"%s\"是否正确。请只回答\"正确\"或\"错误\"。",
                        word, userMeaning
                );

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", "glm-4");
                requestBody.add("messages", gson.toJsonTree(new Object[]{
                        new Message("user", prompt)
                }));

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + getApiKey())
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String content = jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                return content.contains("正确");
            } catch (IOException e) {
                error = "Error checking meaning: " + e.getMessage();
                Log.e(TAG, error, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isCorrect) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onMeaningChecked(isCorrect);
            }
        }
    }

    private class CheckSentenceTask extends AsyncTask<Void, Void, Boolean> {
        private String word;
        private String userSentence;
        private SentenceCheckCallback callback;
        private String error = null;

        public CheckSentenceTask(String word, String userSentence, SentenceCheckCallback callback) {
            this.word = word;
            this.userSentence = userSentence;
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String prompt = String.format(
                        "请判断用户用单词\"%s\"造的句子\"%s\"是否正确。请只回答\"正确\"或\"错误\"。",
                        word, userSentence
                );

                JsonObject requestBody = new JsonObject();
                requestBody.addProperty("model", "glm-4");
                requestBody.add("messages", gson.toJsonTree(new Object[]{
                        new Message("user", prompt)
                }));

                RequestBody body = RequestBody.create(
                        MediaType.parse("application/json"),
                        gson.toJson(requestBody)
                );

                Request request = new Request.Builder()
                        .url(API_URL)
                        .addHeader("Authorization", "Bearer " + API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                JsonObject jsonResponse = gson.fromJson(responseBody, JsonObject.class);
                String content = jsonResponse.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();

                return content.contains("正确");
            } catch (IOException e) {
                error = "Error checking sentence: " + e.getMessage();
                Log.e(TAG, error, e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean isCorrect) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onSentenceChecked(isCorrect);
            }
        }
    }

    private static class Message {
        String role;
        String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    public interface WordInfoCallback {
        void onWordInfoReceived(Word word);
        void onError(String error);
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

    private String getApiKey() {
        return SettingsActivity.getZhipuApiKey(context);
    }
}