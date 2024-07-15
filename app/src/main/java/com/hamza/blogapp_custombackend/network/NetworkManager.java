package com.hamza.blogapp_custombackend.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.hamza.blogapp_custombackend.models.Image;
import com.hamza.blogapp_custombackend.models.Post;
import com.hamza.blogapp_custombackend.models.User;
import com.hamza.blogapp_custombackend.screens.LoginActivity;
import com.hamza.blogapp_custombackend.utils.AppConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkManager {

    private final OkHttpClient client;
    private final TokenManager tokenManager;
    private final Context context;

    public NetworkManager(Context context, TokenManager tokenManager) {
        this.client = new OkHttpClient();
        this.tokenManager = tokenManager;
        this.context = context;
    }

    public interface NetworkCallback<T> {
        void onSuccess(T result);

        void onFailure(String error);
    }

    private void handleInvalidToken() {
        ((Activity) context).runOnUiThread(() -> {
            tokenManager.clearToken();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            ((Activity) context).runOnUiThread(() -> Toast.makeText(context, "Your session has expired", Toast.LENGTH_SHORT).show());
            ((Activity) context).finish();
        });
    }

    public void login(String username, String password, NetworkCallback<String> callback) {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/users/login?username=" + username + "&password=" + password)
                .post(RequestBody.create(null, ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Login failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String token = jsonObject.getString("token");
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(token));
                    } catch (JSONException e) {
                        ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to parse login response"));
                    }
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Login failed: " + response.message()));
                }
            }
        });
    }

    public void registerUser(User user, NetworkCallback<String> callback) {
        Gson gson = new Gson();
        String json = gson.toJson(user);

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), json);
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/users/register")
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Registration failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseBody);

                    if (response.isSuccessful()) {
                        String token = jsonObject.getString("token");
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(token));
                    } else {
                        ((Activity) context).runOnUiThread(() -> {
                            try {
                                callback.onFailure("Registration failed: " + jsonObject.getString("message"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (JSONException e) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to parse registration response"));
                }
            }
        });
    }

    public void loadRoles(String url, NetworkCallback<List<String>> callback) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to load roles: " + e.getMessage()));
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
                            if (!extracted.isEmpty()) {
                                rolesList.add(extracted);
                            }
                        }
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(rolesList));
                    } catch (JSONException e) {
                        ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to parse roles response"));
                    }
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to load roles: " + response.message()));
                }
            }
        });
    }

    public void getPosts(NetworkCallback<List<Post>> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/all")
                .get()
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to load posts: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                    Type listType = new TypeToken<ArrayList<Post>>() {}.getType();
                    List<Post> postList = new Gson().fromJson(jsonObject.getAsJsonArray("content"), listType);
                    ((Activity) context).runOnUiThread(() -> callback.onSuccess(postList));
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to load posts: " + response.message()));
                }
            }
        });
    }

    public void addNewPost(String title, String description, String content, Set<Image> images, NetworkCallback<Post> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Post newPost = new Post(title, description, content, new HashSet<>(), images);
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        String postJson = gson.toJson(newPost);

        RequestBody body = RequestBody.create(postJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/create")
                .post(body)
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to add post: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    Post createdPost = gson.fromJson(jsonResponse, Post.class);
                    ((Activity) context).runOnUiThread(() -> callback.onSuccess(createdPost));
                } else {
                    String errorMessage = "Failed to add post";
                    String errorBody;
                    if (response.body() != null) {
                        try {
                            errorBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                            if (jsonObject.has("title")) {
                                errorMessage = jsonObject.get("title").getAsString();
                            }
                        } catch (IOException ignored) {
                            ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to parse error response"));
                        }
                    }
                    String finalErrorMessage = errorMessage;
                    ((Activity) context).runOnUiThread(() -> callback.onFailure(finalErrorMessage));
                }
            }
        });
    }

    public void editPost(Post post, String title, String description, String content, Set<Image> images, NetworkCallback<Post> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        post.setTitle(title);
        post.setDescription(description);
        post.setContent(content);

        Set<Image> tempImages = new HashSet<>(images);
        post.getImageUrls().clear();
        post.getImageUrls().addAll(tempImages);

        String postJson = new Gson().toJson(post);
        RequestBody body = RequestBody.create(postJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/" + post.getId())
                .put(body)
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to edit post: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    ((Activity) context).runOnUiThread(() -> callback.onSuccess(post));
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to edit post: " + response.message()));
                }
            }
        });
    }

    public void deletePost(Post post, NetworkCallback<Void> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/" + post.getId())
                .delete()
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to delete post: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ((Activity) context).runOnUiThread(() -> callback.onSuccess(null));
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to delete post: " + response.message()));
                }
            }
        });
    }

    public void deleteAllPosts(NetworkCallback<Void> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/all")
                .delete()
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to delete all posts: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    if (responseBody.equals("No posts to delete")) {
                        ((Activity) context).runOnUiThread(() -> callback.onFailure("No posts to delete"));
                    } else if (responseBody.equals("All posts deleted successfully")) {
                        ((Activity) context).runOnUiThread(() -> callback.onSuccess(null));
                    }
                } else {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure("Failed to delete all posts: " + response.message()));
                }
            }
        });
    }
}
