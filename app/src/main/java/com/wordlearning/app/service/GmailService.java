package com.wordlearning.app.service;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePart;
import com.google.api.services.gmail.model.MessagePartBody;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

public class GmailService {
    private static final String TAG = "GmailService";
    private static final String[] SCOPES = {
            "https://www.googleapis.com/auth/gmail.readonly"
    };
    
    private Context context;
    private GoogleAccountCredential credential;
    private Gmail service;
    private WordImportCallback callback;

    public interface WordImportCallback {
        void onWordsImported(List<String> words);
        void onError(String error);
    }

    public GmailService(Context context, WordImportCallback callback) {
        this.context = context;
        this.callback = callback;
        initializeGmailService();
    }

    private void initializeGmailService() {
        credential = GoogleAccountCredential.usingOAuth2(context, Arrays.asList(SCOPES));
        
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType("com.google");
        
        if (accounts.length > 0) {
            credential.setSelectedAccountName(accounts[0].name);
            service = new Gmail.Builder(new NetHttpTransport(), new JacksonFactory(), credential)
                    .setApplicationName("Word Learning App")
                    .build();
        } else {
            callback.onError("No Google account found");
        }
    }

    public void importWordsFromGmail() {
        new ImportWordsTask().execute();
    }

    private class ImportWordsTask extends AsyncTask<Void, Void, List<String>> {
        private String error = null;

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                List<String> words = new ArrayList<>();
                
                ListMessagesResponse response = service.users().messages().list("me")
                        .setQ("subject:Word Learning List")
                        .execute();
                
                List<Message> messages = response.getMessages();
                if (messages == null || messages.isEmpty()) {
                    return words;
                }
                
                for (Message message : messages) {
                    Message fullMessage = service.users().messages().get("me", message.getId()).execute();
                    String emailBody = getMessageBody(fullMessage);
                    
                    List<String> extractedWords = extractWordsFromEmail(emailBody);
                    words.addAll(extractedWords);
                }
                
                return words;
            } catch (UserRecoverableAuthIOException e) {
                error = "Authorization required";
                return null;
            } catch (IOException e) {
                error = "Error reading Gmail: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> words) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onWordsImported(words);
            }
        }
    }

    private String getMessageBody(Message message) throws IOException {
        MessagePart payload = message.getPayload();
        if (payload == null) {
            return "";
        }
        
        if (payload.getBody() != null && payload.getBody().getData() != null) {
            return new String(android.util.Base64.decode(payload.getBody().getData(), android.util.Base64.DEFAULT));
        }
        
        if (payload.getParts() != null) {
            for (MessagePart part : payload.getParts()) {
                if (part.getMimeType().equals("text/plain") && part.getBody() != null) {
                    return new String(android.util.Base64.decode(part.getBody().getData(), android.util.Base64.DEFAULT));
                }
            }
        }
        
        return "";
    }

    private List<String> extractWordsFromEmail(String emailBody) {
        List<String> words = new ArrayList<>();
        
        Pattern wordPattern = Pattern.compile("\\b[a-zA-Z]+\\b");
        Matcher matcher = wordPattern.matcher(emailBody);
        
        while (matcher.find()) {
            String word = matcher.group();
            if (word.length() > 2) {
                words.add(word.toLowerCase());
            }
        }
        
        return words;
    }

    public void requestAuthorization(int requestCode) {
        try {
            credential.chooseAccount();
        } catch (Exception e) {
            Log.e(TAG, "Error requesting authorization", e);
        }
    }
}