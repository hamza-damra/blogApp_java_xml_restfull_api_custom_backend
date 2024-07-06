package com.hamza.blogapp_custombackend.screens;

import static com.hamza.blogapp_custombackend.utils.AppConstant.BASE_URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.utils.KeyboardVisibilityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText nameEditText;
    private MaterialButton registerButton;
    private MaterialButton cancelButton;
    private Spinner roleSpinner;
    private Spinner secondRoleSpinner;
    private List<String> roles = new ArrayList<>();
    private List<String> secondRoles = new ArrayList<>();
    private HintAdapter roleAdapter;
    private HintAdapter secondRoleAdapter;
    private final OkHttpClient client = new OkHttpClient();
    private View cardView;

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
    }

    private void initViews() {
        usernameEditText = findViewById(R.id.re_username);
        passwordEditText = findViewById(R.id.re_password);
        nameEditText = findViewById(R.id.name);
        registerButton = findViewById(R.id.btn_register);
        cancelButton = findViewById(R.id.btn_cancel);
        roleSpinner = findViewById(R.id.role_spinner);
        secondRoleSpinner = findViewById(R.id.second_role_spinner);

        roles.add("Select a role");
        secondRoles.add("Select a second role (optional)");

        roleAdapter = new HintAdapter(this, android.R.layout.simple_spinner_item, roles);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roleSpinner.setAdapter(roleAdapter);

        secondRoleAdapter = new HintAdapter(this, android.R.layout.simple_spinner_item, secondRoles);
        secondRoleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        secondRoleSpinner.setAdapter(secondRoleAdapter);
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
                            roles.clear();
                            roles.add("Select a role");
                            roles.addAll(rolesList);
                            roleAdapter.notifyDataSetChanged();

                            secondRoles.clear();
                            secondRoles.add("Select a second role (optional)");
                            secondRoles.addAll(rolesList);
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
                cardView.animate().translationY(-300).setDuration(300).start(); // Adjust the value as needed
            } else {
                cardView.animate().translationY(0).setDuration(300).start();
            }
        }
    }


    // Custom Adapter Class
    public static class HintAdapter extends ArrayAdapter<String> {
        public HintAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
            super(context, resource, objects);
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