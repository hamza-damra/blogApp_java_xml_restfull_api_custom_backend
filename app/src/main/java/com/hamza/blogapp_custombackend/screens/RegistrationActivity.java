package com.hamza.blogapp_custombackend.screens;

import static com.hamza.blogapp_custombackend.utils.AppConstant.BASE_URL;

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
import com.hamza.blogapp_custombackend.utils.KeyboardVisibilityUtil;
import com.hamza.blogapp_custombackend.validations.TokenManager;
import com.hamza.blogapp_custombackend.validations.Validation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RegistrationActivity extends AppCompatActivity {

    //ui elements
    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private MaterialButton registerButton;
    private MaterialButton cancelButton;
    private Spinner roleSpinner;
    private Spinner secondRoleSpinner;
    private final List<String> role = new ArrayList<>();
    private final List<String> secondRole = new ArrayList<>();
    private HintAdapter roleAdapter;
    private HintAdapter secondRoleAdapter;
    private View cardView;
    private String selectedRole;
    private String selectedSecondRole;
    // api client
    private final OkHttpClient client = new OkHttpClient();
    // token manager
    private TokenManager tokenManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registeration);
        initViews();
        loadRoles(BASE_URL + "/api/roles/all");
        View rootLayout = findViewById(R.id.main);
        cardView = findViewById(R.id.card_view);
        KeyboardVisibilityUtil.setKeyboardVisibilityListener(rootLayout, this::adjustCardPosition);
        cancelButton.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        registerButton.setOnClickListener(v -> {
            registerUser();
        });
    }

    private void registerUser() {
        Gson gson = new Gson();

        String username = Objects.requireNonNull(usernameEditText.getText()).toString();
        String password = Objects.requireNonNull(passwordEditText.getText()).toString();
        String name = Objects.requireNonNull(nameEditText.getText()).toString();

        if (Validation.validateNotEmpty(username) && Validation.validateNotEmpty(password) && Validation.validateNotEmpty(name) && !selectedRole.isEmpty()) {
            String json;
            if (selectedSecondRole.isEmpty() || selectedSecondRole.equals("Select a second role (optional)")) {
                json = gson.toJson(new User(username, password, name, List.of(Role.builder().name(selectedRole).build())));
            } else {
                json = gson.toJson(new User(username, password, name, List.of(Role.builder().name(selectedRole).build(), Role.builder().name(selectedSecondRole).build())));
            }

            RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);

            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/users/register")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    runOnUiThread(() -> Toast.makeText(RegistrationActivity.this, "Failed to register", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    try {
                        String responseBody = Objects.requireNonNull(response.body()).string();
                        JSONObject jsonObject = new JSONObject(responseBody);

                        runOnUiThread(() -> {
                            try {
                                if (response.isSuccessful()) {
                                    tokenManager = new TokenManager(getSharedPreferences("token", MODE_PRIVATE), getSharedPreferences("token", MODE_PRIVATE).edit());
                                    tokenManager.saveToken(jsonObject.getString("token"));
                                    Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                }else if(response.code() == 400){
                                    Toast.makeText(RegistrationActivity.this, "Username already exists", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(RegistrationActivity.this, "Failed to register", Toast.LENGTH_SHORT).show();
                                    Log.e("RegistrationActivity", "Failed to register: " + response.code() + " " + response.message());
                                    Log.e("RegistrationActivity", "Failed to register: " + json);
                                }
                            } catch (JSONException e) {
                                Toast.makeText(RegistrationActivity.this, "Failed to parse JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        Toast.makeText(RegistrationActivity.this, "Failed to parse JSON", Toast.LENGTH_SHORT).show();
                    } finally {
                        response.close();  // Close the response body
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

    private void loadRoles(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(RegistrationActivity.this, "Failed to load roles", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONArray jsonArray = new JSONArray(response.body().string());
                        List<String> rolesList = new ArrayList<>();
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            String extracted = "";
                            String[] parts = jsonObject.getString("name").split("_");
                            if (parts.length > 1) {
                               extracted = parts[1];
                            }
                            if(!extracted.isEmpty()) {
                                rolesList.add(extracted);
                            }
                        }
                        runOnUiThread(() -> {
                            role.clear();
                            role.add("Select a role");
                            role.addAll(rolesList);
                            roleAdapter.notifyDataSetChanged();

                            secondRole.clear();
                            secondRole.add("Select a second role (optional)");
                            secondRole.addAll(rolesList);
                            secondRoleAdapter.notifyDataSetChanged();
                        });
                    } catch (JSONException e) {
                        runOnUiThread(() -> Toast.makeText(RegistrationActivity.this, "Failed to parse roles", Toast.LENGTH_SHORT).show());
                    }
                }
            }
        });
    }

    private void adjustCardPosition(boolean isVisible) {
        if (cardView != null) {
            if (isVisible) {
                cardView.animate().translationY(-300).setDuration(300).start();
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