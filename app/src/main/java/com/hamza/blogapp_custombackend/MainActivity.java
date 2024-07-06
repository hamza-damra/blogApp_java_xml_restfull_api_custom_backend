package com.hamza.blogapp_custombackend;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.hamza.blogapp_custombackend.screens.LoginActivity;
import com.hamza.blogapp_custombackend.screens.RegistrationActivity;
import com.hamza.blogapp_custombackend.validations.TokenManager;

public class MainActivity extends AppCompatActivity {
    private TokenManager tokenManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if(tokenManager.getToken() == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else {
            Intent intent = new Intent(this, RegistrationActivity.class);
            startActivity(intent);
        }

    }
}