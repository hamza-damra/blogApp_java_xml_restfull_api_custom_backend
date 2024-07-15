package com.hamza.blogapp_custombackend.screens;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Role;
import com.hamza.blogapp_custombackend.models.User;
import com.hamza.blogapp_custombackend.network.NetworkManager;
import com.hamza.blogapp_custombackend.utils.AppConstant;
import com.hamza.blogapp_custombackend.utils.KeyboardVisibilityUtil;
import com.hamza.blogapp_custombackend.network.TokenManager;
import com.hamza.blogapp_custombackend.validations.Validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RegistrationActivity extends AppCompatActivity {

    // UI elements
    private TextInputEditText usernameEditText, passwordEditText, nameEditText;
    private MaterialButton registerButton, cancelButton;
    private Spinner roleSpinner, secondRoleSpinner;
    private final List<String> role = new ArrayList<>();
    private final List<String> secondRole = new ArrayList<>();
    private HintAdapter roleAdapter, secondRoleAdapter;
    private View cardView;
    private String selectedRole, selectedSecondRole;
    private NetworkManager networkManager;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);

        initViews();

        tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());
        networkManager = new NetworkManager(this, tokenManager);

        networkManager.loadRoles(AppConstant.BASE_URL + "/api/roles/all", new NetworkManager.NetworkCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> rolesList) {
                role.clear();
                role.add("Select a role");
                role.addAll(rolesList);
                roleAdapter.notifyDataSetChanged();

                secondRole.clear();
                secondRole.add("Select a second role (optional)");
                secondRole.addAll(rolesList);
                secondRoleAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(RegistrationActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });

        View rootLayout = findViewById(R.id.main);
        cardView = findViewById(R.id.card_view);
        KeyboardVisibilityUtil.setKeyboardVisibilityListener(rootLayout, this::adjustCardPosition);

        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String username = Objects.requireNonNull(usernameEditText.getText()).toString();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString();
        String name = Objects.requireNonNull(nameEditText.getText()).toString();

        if (Validation.validateNotEmpty(username) && Validation.validateNotEmpty(password) && Validation.validateNotEmpty(name) && !selectedRole.isEmpty()) {

            if(!Validation.validateLength(username, 3, 30)){
                Toast.makeText(this, "Username must be between 3 and 30 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!Validation.validateLength(password, 8, 30)){
                Toast.makeText(this, "Password must be between 8 and 30 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            if(!Validation.validateLength(name, 3, 20)){
                Toast.makeText(this, "Name must be between 3 and 20 characters", Toast.LENGTH_SHORT).show();
                return;
            }

            User user;
            if (selectedSecondRole.isEmpty() || selectedSecondRole.equals("Select a second role (optional)")) {
                user = new User(username, password, name, List.of(Role.builder().name(selectedRole).build()));
            } else {
                user = new User(username, password, name, List.of(Role.builder().name(selectedRole).build(), Role.builder().name(selectedSecondRole).build()));
            }

            networkManager.registerUser(user, new NetworkManager.NetworkCallback<String>() {
                @Override
                public void onSuccess(String token) {
                    tokenManager.saveToken(token);
                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onFailure(String error) {
                    if (error.contains("400")) {
                        Toast.makeText(RegistrationActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegistrationActivity.this, error, Toast.LENGTH_SHORT).show();
                        Log.e("RegistrationActivity", error);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Please fill out all required fields", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.re_username);
        passwordEditText = findViewById(R.id.re_password);
        nameEditText = findViewById(R.id.name);
        registerButton = findViewById(R.id.btn_register);
        cancelButton = findViewById(R.id.btn_cancel);
        roleSpinner = findViewById(R.id.role_spinner);
        secondRoleSpinner = findViewById(R.id.second_role_spinner);

        role.add("Select a role");
        secondRole.add("Select a second role (optional)");

        roleAdapter = new HintAdapter(this, android.R.layout.simple_spinner_item, role);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        secondRoleAdapter = new HintAdapter(this, android.R.layout.simple_spinner_item, secondRole);
        secondRoleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondRoleSpinner.setAdapter(secondRoleAdapter);

        roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedRole = role.get(i);
                Log.d("selectedRole", selectedRole);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedRole = "";
            }
        });

        secondRoleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedSecondRole = secondRole.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                selectedSecondRole = "";
            }
        });
    }

    private void adjustCardPosition(boolean isVisible) {
        if (cardView != null) {
            if (isVisible) {
                cardView.animate().translationY(+640).setDuration(300).start();
                cardView.animate().translationY(-200).setDuration(300).start();
            } else {
                cardView.animate().translationY(0).setDuration(300).start();
            }
        }
    }

    public static class HintAdapter extends ArrayAdapter<String> {
        public HintAdapter(@NonNull Context context, int resource, @NonNull List<String> roles) {
            super(context, resource, roles);
        }

        @Override
        public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
            if (position == 0) {
                TextView tv = new TextView(getContext());
                tv.setHeight(0);
                tv.setVisibility(View.GONE);
                return tv;
            } else {
                return super.getDropDownView(position, null, parent);
            }
        }
    }
}
