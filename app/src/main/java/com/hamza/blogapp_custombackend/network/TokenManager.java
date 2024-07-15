package com.hamza.blogapp_custombackend.network;

import android.content.SharedPreferences;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.hamza.blogapp_custombackend.models.UserInfo;

import java.util.Date;

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

    public boolean isTokenPresent() {
        return sharedPreferences.contains("token") && !sharedPreferences.getString("token", "").isEmpty();
    }

    public UserInfo getUserInfoFromToken() {
        String token = getToken();
        if (token.isEmpty()) {
            return null;
        }

        try {
            DecodedJWT decodedJWT = JWT.decode(token);
            String username = decodedJWT.getSubject();
            Date issuedAt = decodedJWT.getIssuedAt();
            Date expiresAt = decodedJWT.getExpiresAt();

            return new UserInfo(username, issuedAt, expiresAt);
        } catch (JWTDecodeException e) {
            return null;
        }
    }

    public boolean isTokenValid() {
        UserInfo userInfo = getUserInfoFromToken();
        if (userInfo == null) {
            return false;
        }
        Date now = new Date();
        return userInfo.getExpiresAt().after(now);
    }
}
