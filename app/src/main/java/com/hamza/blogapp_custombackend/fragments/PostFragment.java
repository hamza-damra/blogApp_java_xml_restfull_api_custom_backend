package com.hamza.blogapp_custombackend.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.controllers.ImageUrlAdapter;
import com.hamza.blogapp_custombackend.controllers.PostAdapter;
import com.hamza.blogapp_custombackend.models.Image;
import com.hamza.blogapp_custombackend.models.Post;
import com.hamza.blogapp_custombackend.models.UserInfo;
import com.hamza.blogapp_custombackend.network.NetworkManager;
import com.hamza.blogapp_custombackend.network.TokenManager;
import com.hamza.blogapp_custombackend.screens.LoginActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class PostFragment extends Fragment implements PostAdapter.OnPostActionListener {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> posts;
    private NetworkManager networkManager;
    private RecyclerView rvImageUrls;
    private ImageUrlAdapter imageUrlAdapter;
    private List<String> imageUrls;
    private TokenManager tokenManager;
    private ProgressBar progressBar;
    private int retryCount = 0;
    private final int MAX_RETRY_COUNT = 5;

    public PostFragment() {
    }

    public static PostFragment newInstance() {
        return new PostFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tokenManager = new TokenManager(context.getSharedPreferences("token", Context.MODE_PRIVATE), context.getSharedPreferences("token", Context.MODE_PRIVATE).edit());
        networkManager = new NetworkManager(context, tokenManager);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post, container, false);

        recyclerView = view.findViewById(R.id.postLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        posts = new ArrayList<>();
        postAdapter = new PostAdapter(posts, PostFragment.this);
        recyclerView.setAdapter(postAdapter);

        FloatingActionButton fab = view.findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(v -> showAddPostDialog());

        progressBar = view.findViewById(R.id.progress_bar);
        getPosts();
        startListeningForNewPosts();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.purple_500));
        setHasOptionsMenu(true);
    }

    private void getPosts() {
        progressBar.setVisibility(View.VISIBLE);
        networkManager.getPosts(new NetworkManager.NetworkCallback<List<Post>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Post> result, String... message) {
                posts.clear();
                posts.addAll(result);
                postAdapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), "Posts loaded successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void startListeningForNewPosts() {
        networkManager.startListeningForNewPosts(new NetworkManager.NetworkCallback<Post>() {
            @Override
            public void onSuccess(Post result, String... message) {
                retryCount = 0;
                if (isAdded()) {
                    if (result != null) {
                        if (containsPostWithId(String.valueOf(result.getId()))) {
                            posts.add(result);
                            postAdapter.notifyItemInserted(posts.size() - 1);
                            recyclerView.scrollToPosition(posts.size() - 1);
                            Toast.makeText(requireContext(), "New post added", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("PostFragment", "Duplicate post detected");
                        }
                    } else {
                        Log.e("PostFragment", "Received null post");
                        Toast.makeText(requireContext(), "Failed to receive new post", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(String error) {
                if (isAdded()) {
                    Log.e("PostFragment", "Failed to listen for new posts: " + error);
                    Toast.makeText(requireContext(), "Failed to listen for new posts: " + error, Toast.LENGTH_SHORT).show();
                }
                retryListeningForNewPosts();
            }
        });
    }


    private void stopListeningForNewPosts() {
        networkManager.stopListeningForNewPosts();
    }

    private void retryListeningForNewPosts() {
        if (retryCount < MAX_RETRY_COUNT) {
            final int RETRY_DELAY_MS = (int) Math.pow(2, retryCount) * 1000; // Exponential backoff
            retryCount++;
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isAdded()) {
                    startListeningForNewPosts();
                }
            }, RETRY_DELAY_MS);
        } else {
            Log.e("PostFragment", "Max retry attempts reached. Failed to listen for new posts.");
            if (isAdded()) {
                Toast.makeText(requireContext(), "Failed to listen for new posts after multiple attempts. Please check your connection.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void showAddPostDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_post, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(R.drawable.dialog_background);

        // Initialize UI elements
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
        ProgressBar progressBar = dialogView.findViewById(R.id.progress_bar);
        rvImageUrls = dialogView.findViewById(R.id.rv_image_urls);

        // Set LayoutManager and Adapter
        rvImageUrls.setLayoutManager(new LinearLayoutManager(getContext()));
        imageUrls = new ArrayList<>();
        imageUrls.add("");
        imageUrlAdapter = new ImageUrlAdapter(imageUrls, position -> {
            if (position >= 0 && position < imageUrls.size()) {
                imageUrlAdapter.removeItem(position);
            }
        });
        rvImageUrls.setAdapter(imageUrlAdapter);

        // Show the dialog immediately
        dialog.show();

        // Add image URL button click listener
        TextView btnAddImageUrl = dialogView.findViewById(R.id.btn_add_image_url);
        btnAddImageUrl.setOnClickListener(v -> {
            imageUrls.add("");
            imageUrlAdapter.notifyItemInserted(imageUrls.size() - 1);
        });

        // Cancel button click listener
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Add post button click listener
        btnAdd.setOnClickListener(v -> {
            btnAdd.setEnabled(false); // Disable button to prevent multiple clicks

            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            List<String> collectedImageUrls = new ArrayList<>();
            for (int i = 0; i < imageUrlAdapter.getItemCount(); i++) {
                View view = rvImageUrls.getChildAt(i);
                if (view != null) {
                    TextInputEditText etImageUrl = view.findViewById(R.id.et_image_url);
                    String imageUrl = Objects.requireNonNull(etImageUrl.getText()).toString().trim();
                    if (!imageUrl.isEmpty()) {
                        collectedImageUrls.add(imageUrl);
                    }
                }
            }

            if (!title.isEmpty() && !description.isEmpty() && !content.isEmpty()) {
                Set<Image> imageList = new HashSet<>();
                for (String imageUrl : collectedImageUrls) {
                    imageList.add(new Image(imageUrl));
                }
                progressBar.setVisibility(View.VISIBLE);

                networkManager.addNewPost(title, description, content, imageList, new NetworkManager.NetworkCallback<Post>() {
                    @Override
                    public void onSuccess(Post result, String... message) {
                        if (containsPostWithId(String.valueOf(result.getId()))) {
                            posts.add(result);
                            postAdapter.notifyItemInserted(posts.size() - 1);
                            recyclerView.scrollToPosition(posts.size() - 1);
                            Toast.makeText(requireContext(), "Post added successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("PostFragment", "Duplicate post detected");
                        }
                        progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Log.e("PostFragment", "Failed to add post: " + error);
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnAdd.setEnabled(true); // Re-enable button if validation fails
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean containsPostWithId(String postId) {
        for (Post post : posts) {
            if (String.valueOf(post.getId()).equals(postId)) {
                return false;
            }
        }
        return true;
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

        rvImageUrls = dialogView.findViewById(R.id.rv_image_urls);
        rvImageUrls.setLayoutManager(new LinearLayoutManager(getContext()));

        imageUrls = post.getImageUrls().stream().map(Image::getUrl).collect(Collectors.toList());
        imageUrlAdapter = new ImageUrlAdapter(imageUrls, position -> {
            if (position >= 0 && position < imageUrls.size()) {
                imageUrlAdapter.removeItem(position);
            }
        });
        rvImageUrls.setAdapter(imageUrlAdapter);

        MaterialTextView btnAddImageUrl = dialogView.findViewById(R.id.btn_add_image_url);
        btnAddImageUrl.setOnClickListener(v -> {
            imageUrls.add("");
            imageUrlAdapter.notifyItemInserted(imageUrls.size() - 1);
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());
        btnAdd.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String content = etContent.getText().toString().trim();

            List<String> collectedImageUrls = new ArrayList<>();
            for (int i = 0; i < imageUrlAdapter.getItemCount(); i++) {
                View view = rvImageUrls.getChildAt(i);
                if (view != null) {
                    TextInputEditText etImageUrl = view.findViewById(R.id.et_image_url);
                    String imageUrl = Objects.requireNonNull(etImageUrl.getText()).toString().trim();
                    if (!imageUrl.isEmpty()) {
                        collectedImageUrls.add(imageUrl);
                    }
                }
            }

            if (!title.isEmpty() && !description.isEmpty() && !content.isEmpty()) {
                Set<Image> imageList = new HashSet<>();
                for (String imageUrl : collectedImageUrls) {
                    imageList.add(new Image(imageUrl));
                }
                progressBar.setVisibility(View.VISIBLE);
                networkManager.editPost(post, title, description, content, imageList, new NetworkManager.NetworkCallback<Post>() {
                    @Override
                    public void onSuccess(Post result, String... message) {
                        postAdapter.notifyItemChanged(posts.indexOf(result));
                        Toast.makeText(requireContext(), "Post edited successfully", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onFailure(String error) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
                dialog.dismiss();
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void deletePost(Post post) {
        progressBar.setVisibility(View.VISIBLE);
        networkManager.deletePost(post, new NetworkManager.NetworkCallback<Void>() {
            @Override
            public void onSuccess(Void result, String... message) {
                int position = posts.indexOf(post);
                posts.remove(post);
                postAdapter.notifyItemRemoved(position);
                postAdapter.notifyItemRangeChanged(position, posts.size());
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                Log.d("PostFragment", "Failed to delete post: " + error);
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
        int id = item.getItemId();
        if (id == R.id.action_delete_all) {
            showDeleteConfirmationDialog();
            return true;
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        } else if (id == R.id.action_info) {
            showInfoDialog();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("SetTextI18n")
    private void showInfoDialog() {
        @SuppressLint("InflateParams") View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_user_info, null);

        // Create the BottomSheetDialog
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(dialogView);

        ImageView ivUserImage = dialogView.findViewById(R.id.iv_user_image);
        TextView tvUsername = dialogView.findViewById(R.id.tv_username);
        TextView tvIssuedAt = dialogView.findViewById(R.id.tv_issued_at);
        TextView tvExpiredAt = dialogView.findViewById(R.id.tv_expired_at);

        UserInfo user = tokenManager.getUserInfoFromToken();

        // Set user data
        if (user != null) {
            ivUserImage.setImageResource(R.drawable.ic_user_placeholder);
            tvUsername.setText(user.getUsername());
            tvIssuedAt.setText("Issued At: " + user.getIssuedAt().toString());
            tvExpiredAt.setText("Expired At: " + user.getExpiresAt().toString());
        } else {
            Toast.makeText(requireContext(), "User information is not available", Toast.LENGTH_SHORT).show();
        }
        bottomSheetDialog.show();
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
        progressBar.setVisibility(View.VISIBLE);
        networkManager.deleteAllPosts(new NetworkManager.NetworkCallback<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Void result, String... message) {
                posts.clear();
                postAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "All posts deleted successfully", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logout() {
        tokenManager.clearToken();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }
}
