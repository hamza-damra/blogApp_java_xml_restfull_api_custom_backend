package com.hamza.blogapp_custombackend.screens;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private Spinner roleSpinner;
    private Spinner secondRoleSpinner;
    private List<String> roles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        nameEditText = findViewById(R.id.name);
        roleSpinner = findViewById(R.id.role_spinner);
        secondRoleSpinner = findViewById(R.id.second_role_spinner);

        // Populate the role spinners
        roles = new ArrayList<>();
        roles.add("Admin");
        roles.add("User");
        roles.add("Moderator");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(adapter);
        secondRoleSpinner.setAdapter(adapter);
    }

    public void onRegisterClick(View view) {
        // Handle registration logic
        String username = Objects.requireNonNull(usernameEditText.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString().trim();
        String name = Objects.requireNonNull(nameEditText.getText()).toString().trim();
        String role = roleSpinner.getSelectedItem().toString();
        String secondRole = secondRoleSpinner.getSelectedItem().toString();

        if (username.isEmpty() || password.isEmpty() || name.isEmpty() || role.isEmpty()) {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Perform registration logic here (e.g., send data to server)
        Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
    }

    public void onCancelClick(View view) {
        // Handle cancel logic
        finish();
    }
}
