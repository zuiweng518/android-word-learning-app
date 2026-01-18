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
    private static final String GMAIL_ADDRESS = "gmail_address";
    private static final String GMAIL_PASSWORD = "gmail_password";
    private static final String GMAIL_POP3_SERVER = "gmail_pop3_server";
    private static final String GMAIL_POP3_PORT = "gmail_pop3_port";
    private static final String GMAIL_SMTP_SERVER = "gmail_smtp_server";
    private static final String GMAIL_SMTP_PORT = "gmail_smtp_port";
    private static final String AUTH_METHOD = "auth_method";

    private SharedPreferences prefs;

    private EditText zhipuApiKeyEditText;
    private EditText gmailAddressEditText;
    private EditText gmailPasswordEditText;
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
        gmailAddressEditText = findViewById(R.id.gmailAddressEditText);
        gmailPasswordEditText = findViewById(R.id.gmailPasswordEditText);
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
        String gmailAddress = prefs.getString(GMAIL_ADDRESS, "");
        String gmailPassword = prefs.getString(GMAIL_PASSWORD, "");
        String pop3Server = prefs.getString(GMAIL_POP3_SERVER, "pop.gmail.com");
        String pop3Port = prefs.getString(GMAIL_POP3_PORT, "995");
        String smtpServer = prefs.getString(GMAIL_SMTP_SERVER, "smtp.gmail.com");
        String smtpPort = prefs.getString(GMAIL_SMTP_PORT, "587");
        String authMethod = prefs.getString(AUTH_METHOD, "oauth");

        zhipuApiKeyEditText.setText(zhipuApiKey);
        gmailAddressEditText.setText(gmailAddress);
        gmailPasswordEditText.setText(gmailPassword);
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
        String gmailAddress = gmailAddressEditText.getText().toString().trim();
        String gmailPassword = gmailPasswordEditText.getText().toString().trim();
        String pop3Server = pop3ServerEditText.getText().toString().trim();
        String pop3Port = pop3PortEditText.getText().toString().trim();
        String smtpServer = smtpServerEditText.getText().toString().trim();
        String smtpPort = smtpPortEditText.getText().toString().trim();
        String authMethod = oauthRadioButton.isChecked() ? "oauth" : "password";

        if (TextUtils.isEmpty(zhipuApiKey)) {
            Toast.makeText(this, "请输入智谱AI API密钥", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(gmailAddress)) {
            Toast.makeText(this, "请输入Gmail邮箱地址", Toast.LENGTH_SHORT).show();
            return;
        }

        if ("password".equals(authMethod) && TextUtils.isEmpty(gmailPassword)) {
            Toast.makeText(this, "请输入Gmail密码", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ZHIPU_API_KEY, zhipuApiKey);
        editor.putString(GMAIL_ADDRESS, gmailAddress);
        editor.putString(GMAIL_PASSWORD, gmailPassword);
        editor.putString(GMAIL_POP3_SERVER, pop3Server);
        editor.putString(GMAIL_POP3_PORT, pop3Port);
        editor.putString(GMAIL_SMTP_SERVER, smtpServer);
        editor.putString(GMAIL_SMTP_PORT, smtpPort);
        editor.putString(AUTH_METHOD, authMethod);
        editor.apply();

        Toast.makeText(this, "设置已保存", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void updateAuthMethodUI() {
        boolean isOAuth = oauthRadioButton.isChecked();
        gmailPasswordEditText.setEnabled(!isOAuth);
        pop3ServerEditText.setEnabled(!isOAuth);
        pop3PortEditText.setEnabled(!isOAuth);
        smtpServerEditText.setEnabled(!isOAuth);
        smtpPortEditText.setEnabled(!isOAuth);
    }

    public static String getZhipuApiKey(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(ZHIPU_API_KEY, "");
    }

    public static String getGmailAddress(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_ADDRESS, "");
    }

    public static String getGmailPassword(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_PASSWORD, "");
    }

    public static String getPop3Server(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_POP3_SERVER, "pop.gmail.com");
    }

    public static String getPop3Port(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_POP3_PORT, "995");
    }

    public static String getSmtpServer(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_SMTP_SERVER, "smtp.gmail.com");
    }

    public static String getSmtpPort(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(GMAIL_SMTP_PORT, "587");
    }

    public static String getAuthMethod(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(AUTH_METHOD, "oauth");
    }

    public static boolean isOAuthAuth(Context context) {
        return "oauth".equals(getAuthMethod(context));
    }
}