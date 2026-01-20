package com.wordlearning.app;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AppSettings";
    private static final String ZHIPU_API_KEY = "zhipu_api_key";
    private static final String EMAIL_ADDRESS = "email_address";
    private static final String EMAIL_PASSWORD = "email_password";
    private static final String POP3_SERVER = "pop3_server";
    private static final String POP3_PORT = "pop3_port";
    private static final String SMTP_SERVER = "smtp_server";
    private static final String SMTP_PORT = "smtp_port";
    private static final String AUTH_METHOD = "auth_method";

    private SharedPreferences prefs;

    private EditText zhipuApiKeyEditText;
    private EditText emailAddressEditText;
    private EditText emailPasswordEditText;
    private EditText pop3ServerEditText;
    private EditText pop3PortEditText;
    private EditText smtpServerEditText;
    private EditText smtpPortEditText;
    private RadioGroup authMethodRadioGroup;
    private RadioButton oauthRadioButton;
    private RadioButton passwordRadioButton;
    private Button saveButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSettings();
    }

    private void initViews() {
        zhipuApiKeyEditText = findViewById(R.id.zhipuApiKeyEditText);
        emailAddressEditText = findViewById(R.id.gmailAddressEditText);
        emailPasswordEditText = findViewById(R.id.gmailPasswordEditText);
        pop3ServerEditText = findViewById(R.id.pop3ServerEditText);
        pop3PortEditText = findViewById(R.id.pop3PortEditText);
        smtpServerEditText = findViewById(R.id.smtpServerEditText);
        smtpPortEditText = findViewById(R.id.smtpPortEditText);
        authMethodRadioGroup = findViewById(R.id.authMethodRadioGroup);
        oauthRadioButton = findViewById(R.id.oauthRadioButton);
        passwordRadioButton = findViewById(R.id.passwordRadioButton);
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> saveSettings());
        cancelButton.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        String zhipuApiKey = prefs.getString(ZHIPU_API_KEY, "");
        String emailAddress = prefs.getString(EMAIL_ADDRESS, "");
        String emailPassword = prefs.getString(EMAIL_PASSWORD, "");
        String pop3Server = prefs.getString(POP3_SERVER, "pop.126.com");
        String pop3Port = prefs.getString(POP3_PORT, "110");
        String smtpServer = prefs.getString(SMTP_SERVER, "smtp.126.com");
        String smtpPort = prefs.getString(SMTP_PORT, "25");
        String authMethod = prefs.getString(AUTH_METHOD, "password");

        zhipuApiKeyEditText.setText(zhipuApiKey);
        emailAddressEditText.setText(emailAddress);
        emailPasswordEditText.setText(emailPassword);
        pop3ServerEditText.setText(pop3Server);
        pop3PortEditText.setText(pop3Port);
        smtpServerEditText.setText(smtpServer);
        smtpPortEditText.setText(smtpPort);

        if ("oauth".equals(authMethod)) {
            oauthRadioButton.setChecked(true);
        } else {
            passwordRadioButton.setChecked(true);
        }

        updateAuthMethodUI();
    }

    private void saveSettings() {
        String zhipuApiKey = zhipuApiKeyEditText.getText().toString().trim();
        String emailAddress = emailAddressEditText.getText().toString().trim();
        String emailPassword = emailPasswordEditText.getText().toString().trim();
        String pop3Server = pop3ServerEditText.getText().toString().trim();
        String pop3Port = pop3PortEditText.getText().toString().trim();
        String smtpServer = smtpServerEditText.getText().toString().trim();
        String smtpPort = smtpPortEditText.getText().toString().trim();
        String authMethod = passwordRadioButton.isChecked() ? "password" : "oauth";

        if (TextUtils.isEmpty(zhipuApiKey)) {
            Toast.makeText(this, "请输入智谱AI API密钥", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(emailAddress)) {
            Toast.makeText(this, "请输入邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("password".equals(authMethod) && TextUtils.isEmpty(emailPassword)) {
            Toast.makeText(this, "请输入邮箱密码", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ZHIPU_API_KEY, zhipuApiKey);
        editor.putString(EMAIL_ADDRESS, emailAddress);
        editor.putString(EMAIL_PASSWORD, emailPassword);
        editor.putString(POP3_SERVER, pop3Server);
        editor.putString(POP3_PORT, pop3Port);
        editor.putString(SMTP_SERVER, smtpServer);
        editor.putString(SMTP_PORT, smtpPort);
        editor.putString(AUTH_METHOD, authMethod);
        editor.apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateAuthMethodUI() {
        boolean isOAuth = oauthRadioButton.isChecked();
        emailPasswordEditText.setEnabled(!isOAuth);
        pop3ServerEditText.setEnabled(!isOAuth);
        pop3PortEditText.setEnabled(!isOAuth);
        smtpServerEditText.setEnabled(!isOAuth);
        smtpPortEditText.setEnabled(!isOAuth);
    }

    public static String getZhipuApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(ZHIPU_API_KEY, "");
    }

    public static String getEmailAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(EMAIL_ADDRESS, "");
    }

    public static String getEmailPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(EMAIL_PASSWORD, "");
    }

    public static String getPop3Server(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(POP3_SERVER, "pop.126.com");
    }

    public static String getPop3Port(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(POP3_PORT, "110");
    }

    public static String getSmtpServer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(SMTP_SERVER, "smtp.126.com");
    }

    public static String getSmtpPort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(SMTP_PORT, "25");
    }

    public static String getAuthMethod(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(AUTH_METHOD, "password");
    }

    public static boolean isOAuthAuth(Context context) {
        return "oauth".equals(getAuthMethod(context));
    }
}