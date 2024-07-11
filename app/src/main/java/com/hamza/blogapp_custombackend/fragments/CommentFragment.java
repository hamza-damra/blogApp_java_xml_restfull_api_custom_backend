package com.hamza.blogapp_custombackend.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Post;

public class CommentFragment extends Fragment {

    private static final String ARG_POST = "post";
    private Post post;

    public CommentFragment() {
    }

    public static CommentFragment newInstance(Post post) {
        CommentFragment fragment = new CommentFragment();
        Bundle args = new Bundle();
        args.putString(ARG_POST, new Gson().toJson(post));
        fragment.setArguments(args);
        return fragment;
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
        return inflater.inflate(R.layout.fragment_comment, container, false);
    }
}
