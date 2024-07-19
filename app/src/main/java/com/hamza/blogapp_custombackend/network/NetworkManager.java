package com.hamza.blogapp_custombackend.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.hamza.blogapp_custombackend.models.Comment;
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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.EventListener;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;

public class NetworkManager {

    private final OkHttpClient client;
    private final TokenManager tokenManager;
    private final Context context;
    private EventSource eventSource;
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = 5;
    private Activity activity;

    public NetworkManager(Context context, TokenManager tokenManager) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(300, TimeUnit.HOURS)
                .writeTimeout(300, TimeUnit.HOURS)
                .readTimeout(300, TimeUnit.HOURS)
                .build();
        this.tokenManager = tokenManager;
        this.context = context;
        if(context instanceof Activity){
            this.activity = (Activity) context;
        }

    }

    // get Instance
    public static NetworkManager getInstance(Context context, TokenManager tokenManager) {
        return new NetworkManager(context, tokenManager);
    }

    public interface NetworkCallback<T> {
        void onSuccess(T result, String... message);

        void onFailure(String error);
    }

    private void handleInvalidToken() {
        runOnUiThread(() -> {
            tokenManager.clearToken();
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            Toast.makeText(context, "Your session has expired", Toast.LENGTH_SHORT).show();
            if(activity!= null)
            {
                activity.finish();
            }else {
               throw new IllegalStateException("Activity not found");
            }
        });
    }

    private void runOnUiThread(Runnable action) {
        if(activity != null){
            activity.runOnUiThread(action);
        }else {
            throw new IllegalStateException("Activity not found");
        }
    }

    public void login(String username, String password, NetworkCallback<String> callback) {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/users/login?username=" + username + "&password=" + password)
                .post(RequestBody.create(null, ""))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> callback.onFailure("Login failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        String token = jsonObject.getString("token");
                        runOnUiThread(() -> callback.onSuccess(token));
                    } catch (JSONException e) {
                        runOnUiThread(() -> callback.onFailure("Failed to parse login response"));
                    }
                } else {
                    runOnUiThread(() -> callback.onFailure("Login failed: " + response.message()));
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
                runOnUiThread(() -> callback.onFailure("Registration failed: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try {
                    String responseBody = Objects.requireNonNull(response.body()).string();
                    JSONObject jsonObject = new JSONObject(responseBody);

                    if (response.isSuccessful()) {
                        String token = jsonObject.getString("token");
                        runOnUiThread(() -> callback.onSuccess(token));
                    } else {
                        runOnUiThread(() -> {
                            try {
                                callback.onFailure("Registration failed: " + jsonObject.getString("username"));
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
                            }
                        });
                    }
                } catch (JSONException e) {
                    runOnUiThread(() -> callback.onFailure("Failed to parse registration response"));
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
                Log.d("NetworkService", "Failed to load roles", e);
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
                        runOnUiThread(() -> callback.onSuccess(rolesList));
                    } catch (JSONException e) {
                        runOnUiThread(() -> callback.onFailure("Failed to parse roles response"));
                    }
                } else {
                    Log.d("NetworkService", "Failed to load roles: " + response.message());
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
               Log.d("NetworkService", "Failed to load posts", e);
               handleInvalidToken();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                    Type listType = new TypeToken<ArrayList<Post>>() {}.getType();
                    List<Post> postList = new Gson().fromJson(jsonObject.getAsJsonArray("content"), listType);
                    runOnUiThread(() -> callback.onSuccess(postList));
                } else {
                    Log.d("NetworkService", "Failed to load posts: " + response.message());
                    handleInvalidToken();
                }
            }
        });
    }

    public void startListeningForNewPosts(NetworkCallback<Post> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/sse")
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        eventSource = EventSources.createFactory(client).newEventSource(request, new EventSourceListener() {
            @Override
            public void onOpen(@NonNull EventSource eventSource, @NonNull Response response) {
                Log.d("SSE", "Connection opened");
            }

            @Override
            public void onEvent(@NonNull EventSource eventSource, @Nullable String id, @Nullable String type, @NonNull String data) {
                Log.d("SSE", "New event received: " + data);
                Post post = new Gson().fromJson(data, Post.class);
                runOnUiThread(() -> callback.onSuccess(post));
            }

            @Override
            public void onClosed(@NonNull EventSource eventSource) {
                Log.d("SSE", "Connection closed");
            }

            @Override
            public void onFailure(@NonNull EventSource eventSource, @Nullable Throwable t, @Nullable Response response) {
                Log.e("SSE", "Connection failed", t);
            }
        });
    }


    public void stopListeningForNewPosts() {
        if (eventSource != null) {
            eventSource.cancel();
        }
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
                Log.d("NetworkService", "Failed to add post", e);
                runOnUiThread(() -> callback.onFailure("Failed to add post: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Post createdPost = gson.fromJson(jsonResponse, Post.class);
                        runOnUiThread(() -> callback.onSuccess(createdPost));
                    } catch (JsonSyntaxException e) {
                        Log.e("NetworkService", "Failed to parse JSON response", e);
                        handleInvalidToken();

                    }
                } else {
                    String errorMessage = "Failed to add post";
                    if (response.body() != null) {
                        try {
                            String errorBody = response.body().string();
                            JsonObject jsonObject = JsonParser.parseString(errorBody).getAsJsonObject();
                            if (jsonObject.has("title")) {
                                errorMessage = jsonObject.get("title").getAsString();
                            }
                        } catch (IOException | JsonSyntaxException ignored) {
                            runOnUiThread(() -> callback.onFailure("Failed to parse error response"));
                        }
                    }
                    String finalErrorMessage = errorMessage;
                    runOnUiThread(() -> callback.onFailure(finalErrorMessage));
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
                runOnUiThread(() -> callback.onFailure("Failed to edit post: " + e.getMessage()));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    runOnUiThread(() -> callback.onSuccess(post));
                } else {
                    runOnUiThread(() -> callback.onFailure("Failed to edit post: " + response.message()));
                    Log.d("PostFragment", "Failed to edit post: "+response.message());
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
                runOnUiThread(() -> callback.onFailure("Failed to delete post: " + e.getMessage()));
                Log.d("PostFragment", "Failed to delete post: "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> callback.onSuccess(null));
                } else {
                    runOnUiThread(() -> callback.onFailure("Failed to delete post: " + response.message()));
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
                runOnUiThread(() -> callback.onFailure("Failed to delete all posts: " + e.getMessage()));
                Log.d("PostFragment", "Failed to delete all posts: "+e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    assert response.body() != null;
                    String responseBody = response.body().string();
                    if (responseBody.equals("No posts to delete")) {
                        runOnUiThread(() -> callback.onFailure("No posts to delete"));
                    } else if (responseBody.equals("All posts deleted successfully")) {
                        runOnUiThread(() -> callback.onSuccess(null));
                    }
                } else {
                    runOnUiThread(() -> callback.onFailure("Failed to delete all posts: " + response.message()));
                    Log.d("PostFragment", "Failed to delete all posts: "+response);
                }
            }
        });
    }

    public void getComments(int postId, NetworkCallback<List<Comment>> callback) {
        if (!tokenManager.isTokenValid()) {
            handleInvalidToken();
            return;
        }

        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/comments/post/" + postId)
                .get()
                .addHeader("Authorization", "Bearer " + tokenManager.getToken())
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("NetworkService", "Failed to load comments", e);
                runOnUiThread(() -> callback.onFailure("Failed to load comments. Please check your connection."));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    Gson gson = new Gson();
                    JsonObject jsonObject = gson.fromJson(jsonResponse, JsonObject.class);
                    JsonArray jsonArray = jsonObject.getAsJsonArray("content");

                    if (jsonArray == null || jsonArray.size() == 0) {
                        runOnUiThread(() -> callback.onSuccess(new ArrayList<>(), "No comments found."));
                        return;
                    }

                    Type commentListType = new TypeToken<List<Comment>>() {}.getType();
                    List<Comment> comments = gson.fromJson(jsonArray, commentListType);
                    runOnUiThread(() -> callback.onSuccess(comments, "Comments loaded successfully"));
                    Log.d("NetworkService", "Comments: " + comments);
                } else {
                    runOnUiThread(() -> callback.onFailure("Failed to load comments: " + response.message()));
                }
            }
        });
    }
}
