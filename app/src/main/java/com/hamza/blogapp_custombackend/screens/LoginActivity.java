package com.hamza.blogapp_custombackend.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.network.NetworkManager;
import com.hamza.blogapp_custombackend.utils.KeyboardVisibilityUtil;
import com.hamza.blogapp_custombackend.network.TokenManager;
import com.hamza.blogapp_custombackend.validations.Validation;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText username, password;
    MaterialButton button_login;
    TextView tv_forgot_password;
    private TokenManager tokenManager;
    private NetworkManager networkManager;
    private View cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());
        networkManager = new NetworkManager(this, tokenManager);

        initViews();

        tv_forgot_password.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        button_login.setOnClickListener(v -> {
            if (Validation.validateNotEmpty(Objects.requireNonNull(username.getText()).toString()) && Validation.validateNotEmpty(Objects.requireNonNull(password.getText()).toString())) {
                String user = Objects.requireNonNull(username.getText()).toString();
                String pass = Objects.requireNonNull(password.getText()).toString();
                login(user, pass);
            } else {
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            }
        });

        View rootLayout = findViewById(R.id.main);
        cardView = findViewById(R.id.card_view);
        KeyboardVisibilityUtil.setKeyboardVisibilityListener(rootLayout, this::adjustCardPosition);
    }

    private void initViews() {
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        button_login = findViewById(R.id.btn_login);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);
    }

    private void login(String username, String password) {
        networkManager.login(username, password, new NetworkManager.NetworkCallback<String>() {
            @Override
            public void onSuccess(String token) {
                tokenManager.saveToken(token);
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show());
                Log.e("Login", error);
            }
        });
    }

    private void adjustCardPosition(boolean isVisible) {
        if (cardView != null) {
            if (isVisible) {
                cardView.animate().translationY(+20).setDuration(300).start(); // Adjust the value as needed
            } else {
                cardView.animate().translationY(0).setDuration(300).start();
            }
        }
    }
}
