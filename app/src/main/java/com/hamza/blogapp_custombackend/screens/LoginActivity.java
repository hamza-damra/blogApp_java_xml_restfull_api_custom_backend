package com.hamza.blogapp_custombackend.screens;

import static com.hamza.blogapp_custombackend.utils.AppConstant.BASE_URL;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.utils.KeyboardVisibilityUtil;
import com.hamza.blogapp_custombackend.validations.TokenManager;
import com.hamza.blogapp_custombackend.validations.Validation;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    TextInputEditText username, password;
    MaterialButton button_login;
    TextView tv_forgot_password;
    private final OkHttpClient client = new OkHttpClient();
    private TokenManager tokenManager;
    private View cardView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initViews();

        tv_forgot_password.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        button_login.setOnClickListener(v -> {
            if(Validation.validateNotEmpty(Objects.requireNonNull(username.getText()).toString()) && Validation.validateNotEmpty(Objects.requireNonNull(password.getText()).toString())) {
                String user = Objects.requireNonNull(username.getText()).toString();
                String pass = Objects.requireNonNull(password.getText()).toString();
                login(user, pass);
            }else{
                Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            }
        });

        View rootLayout = findViewById(R.id.main);
        cardView = findViewById(R.id.card_view);
        KeyboardVisibilityUtil.setKeyboardVisibilityListener((rootLayout), new KeyboardVisibilityUtil.KeyboardVisibilityListener() {
            @Override
            public void onKeyboardVisibilityChanged(boolean isVisible) {
                adjustCardPosition(isVisible);
            }
        });
    }

    private void initViews() {
        username = findViewById(R.id.et_username);
        password = findViewById(R.id.et_password);
        button_login = findViewById(R.id.btn_login);
        tv_forgot_password = findViewById(R.id.tv_forgot_password);
    }

    private void login(String username, String password) {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/users/login?username=" + username + "&password=" + password)
                .post(RequestBody.create(null, ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                Log.e("Login", "Login failed".concat(e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String token = jsonObject.getString("token");
                        runOnUiThread(() -> {
                            tokenManager.saveToken(token);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                            Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    private void adjustCardPosition(boolean isVisible) {
        if (cardView != null) {
            if (isVisible) {
                cardView.animate().translationY(-200).setDuration(300).start(); // Adjust the value as needed
            } else {
                cardView.animate().translationY(0).setDuration(300).start();
            }
        }
    }
}
