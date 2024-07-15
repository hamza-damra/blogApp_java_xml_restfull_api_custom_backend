package com.hamza.blogapp_custombackend.screens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentTransaction;

import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.fragments.PostFragment;
import com.hamza.blogapp_custombackend.models.UserInfo;
import com.hamza.blogapp_custombackend.network.TokenManager;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Posts Screen");

        tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());

        if (!isTokenValid()) {
            navigateToLogin();
        } else {
            loadPostFragment();
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isTokenValid() {
        if (tokenManager.isTokenPresent()) {
            UserInfo userInfo = tokenManager.getUserInfoFromToken();
            if (userInfo != null) {
                Log.d("Token", userInfo.getUsername());
                Log.d("Token", userInfo.getIssuedAt().toString());
                Log.d("Token", userInfo.getExpiresAt().toString());
                return tokenManager.isTokenValid();
            }
        }
        return false;
    }

    private void navigateToLogin() {
        tokenManager.clearToken();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadPostFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, PostFragment.newInstance());
        transaction.commit();
    }
}
