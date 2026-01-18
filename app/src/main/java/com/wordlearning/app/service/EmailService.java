package com.wordlearning.app.service;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class EmailService {
    private static final String TAG = "EmailService";
    
    private Context context;
    private EmailImportCallback callback;

    public interface EmailImportCallback {
        void onEmailsImported(List<String> words);
        void onError(String error);
    }

    public EmailService(Context context, EmailImportCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void importWordsFromEmail() {
        new ImportWordsTask().execute();
    }

    private class ImportWordsTask extends android.os.AsyncTask<Void, Void, List<String>> {
        private String error = null;

        @Override
        protected List<String> doInBackground(Void... voids) {
            try {
                List<String> words = new ArrayList<>();
                
                String email = SettingsActivity.getGmailAddress(context);
                String password = SettingsActivity.getGmailPassword(context);
                String pop3Server = SettingsActivity.getPop3Server(context);
                String pop3Port = SettingsActivity.getPop3Port(context);
                
                if (SettingsActivity.isOAuthAuth(context)) {
                    error = "OAuth认证暂不支持，请使用密码认证";
                    return null;
                }
                
                if (email.isEmpty() || password.isEmpty()) {
                    error = "请先在设置中配置邮箱地址和密码";
                    return null;
                }
                
                Properties props = new Properties();
                props.put("mail.pop3.host", pop3Server);
                props.put("mail.pop3.port", pop3Port);
                props.put("mail.pop3.auth", "true");
                props.put("mail.pop3.ssl.enable", "true");
                
                Session session = Session.getInstance(props);
                Store store = session.getStore("pop3s");
                
                store.connect(email, password);
                Folder inbox = store.getFolder("INBOX");
                inbox.open(Folder.READ_ONLY);
                
                Message[] messages = inbox.getMessages();
                
                for (Message message : messages) {
                    String subject = message.getSubject();
                    if (subject != null && subject.contains("Word Learning List")) {
                        String content = message.getContent().toString();
                        List<String> extractedWords = extractWordsFromEmail(content);
                        words.addAll(extractedWords);
                    }
                }
                
                inbox.close(false);
                store.close();
                
                return words;
            } catch (Exception e) {
                error = "Error importing from email: " + e.getMessage();
                Log.e(TAG, error, e);
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<String> words) {
            if (error != null) {
                callback.onError(error);
            } else {
                callback.onEmailsImported(words);
            }
        }
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

    public boolean sendTestEmail(String to, String subject, String content) {
        try {
            String email = SettingsActivity.getGmailAddress(context);
            String password = SettingsActivity.getGmailPassword(context);
            String smtpServer = SettingsActivity.getSmtpServer(context);
            String smtpPort = SettingsActivity.getSmtpPort(context);
            
            if (email.isEmpty() || password.isEmpty()) {
                return false;
            }
            
            Properties props = new Properties();
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");
            props.put("mail.smtp.host", smtpServer);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.ssl.enable", "true");
            
            Session session = Session.getInstance(props);
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(email));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);
            message.setText(content);
            
            Transport transport = session.getTransport("smtp");
            transport.connect(email, password);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
            
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error sending email", e);
            return false;
        }
    }
}