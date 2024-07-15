package com.hamza.blogapp_custombackend.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.controllers.ImageUrlAdapter;
import com.hamza.blogapp_custombackend.controllers.PostAdapter;
import com.hamza.blogapp_custombackend.models.Image;
import com.hamza.blogapp_custombackend.models.Post;
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
        getPosts();
        postAdapter = new PostAdapter(posts, PostFragment.this);
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
        networkManager.getPosts(new NetworkManager.NetworkCallback<List<Post>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Post> result) {
                posts.clear();
                posts.addAll(result);
                postAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "Posts loaded successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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

        // Initialize UI elements
        EditText etTitle = dialogView.findViewById(R.id.et_title);
        EditText etDescription = dialogView.findViewById(R.id.et_description);
        EditText etContent = dialogView.findViewById(R.id.et_content);
        Button btnCancel = dialogView.findViewById(R.id.btn_cancel);
        Button btnAdd = dialogView.findViewById(R.id.btn_add);
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
        MaterialTextView btnAddImageUrl = dialogView.findViewById(R.id.btn_add_image_url);
        btnAddImageUrl.setOnClickListener(v -> {
            imageUrls.add("");
            imageUrlAdapter.notifyItemInserted(imageUrls.size() - 1);
        });

        // Cancel button click listener
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        // Add post button click listener
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
                networkManager.addNewPost(title, description, content, imageList, new NetworkManager.NetworkCallback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        posts.add(result);
                        postAdapter.notifyItemInserted(posts.size() - 1);
                        recyclerView.scrollToPosition(posts.size() - 1);
                        Toast.makeText(requireContext(), "Post added successfully", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }

                    @Override
                    public void onFailure(String error) {
                        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
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
                networkManager.editPost(post, title, description, content, imageList, new NetworkManager.NetworkCallback<Post>() {
                    @Override
                    public void onSuccess(Post result) {
                        postAdapter.notifyItemChanged(posts.indexOf(result));
                        Toast.makeText(requireContext(), "Post edited successfully", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
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
        networkManager.deletePost(post, new NetworkManager.NetworkCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                int position = posts.indexOf(post);
                posts.remove(post);
                postAdapter.notifyItemRemoved(position);
                postAdapter.notifyItemRangeChanged(position, posts.size());
                Toast.makeText(requireContext(), "Post deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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
        } else {
            return super.onOptionsItemSelected(item);
        }
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
        networkManager.deleteAllPosts(new NetworkManager.NetworkCallback<Void>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(Void result) {
                posts.clear();
                postAdapter.notifyDataSetChanged();
                Toast.makeText(requireContext(), "All posts deleted successfully", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
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
