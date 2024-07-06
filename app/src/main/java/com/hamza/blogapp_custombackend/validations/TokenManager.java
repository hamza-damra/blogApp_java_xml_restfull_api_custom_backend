package com.hamza.blogapp_custombackend.validations;

import android.content.SharedPreferences;

public class TokenManager {

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public TokenManager(SharedPreferences sharedPreferences, SharedPreferences.Editor editor) {
        this.sharedPreferences = sharedPreferences;
        this.editor = editor;
    }

    public void saveToken(String token) {
        editor.putString("token", token);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString("token", "");
    }

    public void clearToken() {
        editor.remove("token");
        editor.apply();
    }


}
