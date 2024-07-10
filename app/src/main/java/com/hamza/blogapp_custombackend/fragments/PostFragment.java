package com.hamza.blogapp_custombackend.fragments;

import static com.hamza.blogapp_custombackend.utils.AppConstant.ACCESS_TOKEN;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PostFragment extends Fragment implements PostAdapter.OnPostActionListener {

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
        postAdapter = new PostAdapter(posts, requireContext(), this);
        recyclerView.setAdapter(postAdapter);
        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> showAddPostDialog());
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.purple_500));
        setHasOptionsMenu(true);
    }

    private void getPosts() {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/all")
                .get()
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
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
                    Type listType = new TypeToken<ArrayList<Post>>() {
                    }.getType();
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
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

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
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
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

    @Override
    public void onPostEdit(Post post) {
        showEditPostDialog(post);
    }

    @Override
    public void onPostDelete(Post post) {
        deletePost(post);
    }

    private void showEditPostDialog(Post post) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);

        etTitle.setText(post.getTitle());
        etDescription.setText(post.getDescription());
        etContent.setText(post.getContent());

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            if (!title.isEmpty() && !description.isEmpty() && !content.isEmpty()) {
                editPost(post, title, description, content);
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void editPost(Post post, String title, String description, String content) {
        post.setTitle(title);
        post.setDescription(description);
        post.setContent(content);
        String postJson = new Gson().toJson(post);

        RequestBody body = RequestBody.create(postJson, MediaType.get("application/json; charset=utf-8"));
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/" + post.getId())
                .put(body)
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Failed to edit post: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to edit post", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    requireActivity().runOnUiThread(() -> {
                        postAdapter.notifyItemChanged(posts.indexOf(post));
                        Toast.makeText(requireContext(), "Post edited successfully", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    private void deletePost(Post post) {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/" + post.getId())
                .delete()
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Failed to delete post: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to delete post", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        posts.remove(post);
                        postAdapter.notifyItemRemoved(posts.indexOf(post));
                        Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.action_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all) {
            showDeleteConfirmationDialog();
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Delete All Posts")
                .setMessage("Are you sure you want to delete all posts?")
                .setPositiveButton("Delete", (dialog, which) -> deleteAllPosts())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create();

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);
        dialog.show();

    }


    private void deleteAllPosts() {
        Request request = new Request.Builder()
                .url(AppConstant.BASE_URL + "/api/posts/all")
                .delete()
                .addHeader("Authorization", "Bearer " + ACCESS_TOKEN)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e("Error", "Failed to delete all posts: " + e.getMessage());
                requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to delete all posts", Toast.LENGTH_SHORT).show());
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    requireActivity().runOnUiThread(() -> {
                        try {
                            assert response.body() != null;
                            String responseBody = response.body().string();
                            if(responseBody.equals("No posts to delete"))
                            {
                                Toast.makeText(requireContext(), "No posts to delete", Toast.LENGTH_SHORT).show();
                                return;
                            }else {
                                    if(responseBody.equals("All posts deleted successfully")) {
                                        posts.clear();
                                        postAdapter.notifyDataSetChanged();
                                        Toast.makeText(requireContext(), "All posts deleted successfully", Toast.LENGTH_SHORT).show();
                                    }
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                    });
                } else {
                    requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Failed to delete all posts", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

}
