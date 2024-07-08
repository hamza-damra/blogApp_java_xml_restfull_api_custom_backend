package com.hamza.blogapp_custombackend.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.controllers.PostAdapter;
import com.hamza.blogapp_custombackend.models.Post;
import com.hamza.blogapp_custombackend.utils.AppConstant;
import com.hamza.blogapp_custombackend.validations.TokenManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private final OkHttpClient client = new OkHttpClient();
    private TokenManager tokenManager;

    public PostFragment() {
    }

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tokenManager = new TokenManager(context.getSharedPreferences("token", Context.MODE_PRIVATE), context.getSharedPreferences("token", Context.MODE_PRIVATE).edit());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerView = view.findViewById(R.id.postLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        posts = new ArrayList<>();
        getPosts();
        postAdapter = new PostAdapter(posts, getContext());
        recyclerView.setAdapter(postAdapter);
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> showAddPostDialog());
        return view;
    }

    private void getPosts() {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/all")
                .get()
                .addHeader("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dGVzMXQiLCJpYXQiOjE3MjA0NDcxMDYsImV4cCI6MTcyMDUzMzUwNn0.HkhZcBZtnNSLbhnpsT8dGW43ASI26sFLk4_BbRy3oGWEhe5TgMlyRG1M-sL2b26vgygZ8k1xjvHzQ50TnKn2wg")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Failed to load posts: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to load posts", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();

                    // Parse the JSON response
                    JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
                    Type listType = new TypeToken<ArrayList<Post>>() {}.getType();
                    List<Post> postList = new Gson().fromJson(jsonObject.getAsJsonArray("content"), listType);

                    requireActivity().runOnUiThread(() -> {
                        posts.clear();
                        posts.addAll(postList);
                        postAdapter.notifyDataSetChanged();
                        Toast.makeText(requireContext(), "Posts loaded successfully", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void showAddPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);

        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (!title.isEmpty() && !description.isEmpty() && !content.isEmpty()) {
                addNewPost(title, description, content);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }


    private void addNewPost(String title, String description, String content) {
        Post newPost = new Post(title, description, content, new ArrayList<>());
        String postJson = new Gson().toJson(newPost);

        RequestBody body = RequestBody.create(postJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/create")
                .post(body)
                .addHeader("Authorization", "Bearer " + "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0ZXN0dGVzMXQiLCJpYXQiOjE3MjA0NDcxMDYsImV4cCI6MTcyMDUzMzUwNn0.HkhZcBZtnNSLbhnpsT8dGW43ASI26sFLk4_BbRy3oGWEhe5TgMlyRG1M-sL2b26vgygZ8k1xjvHzQ50TnKn2wg")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Failed to add post: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to add post", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonResponse = response.body().string();
                    Post createdPost = new Gson().fromJson(jsonResponse, Post.class);

                    requireActivity().runOnUiThread(() -> {
                        posts.add(createdPost);
                        postAdapter.notifyItemInserted(posts.size() - 1);
                        recyclerView.scrollToPosition(posts.size() - 1);
                        Toast.makeText(requireContext(), "Post added successfully", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
