package com.hamza.blogapp_custombackend.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.controllers.CommentAdapter;
import com.hamza.blogapp_custombackend.models.Comment;
import com.hamza.blogapp_custombackend.models.Post;
import com.hamza.blogapp_custombackend.network.NetworkManager;
import com.hamza.blogapp_custombackend.network.TokenManager;

import java.util.ArrayList;
import java.util.List;

public class CommentFragment extends Fragment {

    private static final String ARG_POST = "post";
    private Post post;
    private List<Comment> comments;
    private RecyclerView recyclerView;
    private TokenManager tokenManager;
    private NetworkManager networkManager;
    private CommentAdapter commentAdapter;
    private ProgressBar progressBar;

    public CommentFragment() {
        // Required empty public constructor
    }

    public static CommentFragment newInstance(Post post) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST, new Gson().toJson(post));
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        tokenManager = TokenManager.getInstance(context.getSharedPreferences("token", Context.MODE_PRIVATE), context.getSharedPreferences("token", Context.MODE_PRIVATE).edit());
        networkManager = NetworkManager.getInstance(context, tokenManager);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String postJson = getArguments().getString(ARG_POST);
            post = new Gson().fromJson(postJson, Post.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_comment, container, false);
        recyclerView = view.findViewById(R.id.commentLayout);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setHasFixedSize(true);
        comments = new ArrayList<>();
        commentAdapter = new CommentAdapter(comments, requireContext());
        recyclerView.setAdapter(commentAdapter);
        progressBar = view.findViewById(R.id.progress_bar);
        getComments();
        return view;
    }

    private void getComments() {
        progressBar.setVisibility(View.VISIBLE);
        networkManager.getComments(post.getId(), new NetworkManager.NetworkCallback<List<Comment>>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(List<Comment> commentsData, String... message) {
                progressBar.setVisibility(View.GONE);
                comments.clear();
                comments.addAll(commentsData);
                commentAdapter.notifyDataSetChanged();
                if (message.length > 0 && message[0] != null && !message[0].isEmpty()) {
                    Toast.makeText(requireContext(), message[0], Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(requireContext(), "Comments loaded successfully", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(String message) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
