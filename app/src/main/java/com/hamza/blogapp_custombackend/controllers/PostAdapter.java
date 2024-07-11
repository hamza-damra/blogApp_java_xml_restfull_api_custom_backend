package com.hamza.blogapp_custombackend.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Post;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private final List<Post> posts;
    private final OnPostActionListener listener;

    public PostAdapter(List<Post> posts, OnPostActionListener listener) {
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_custom_card_view, parent, false);
        return new PostViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public interface OnPostActionListener {
        void onPostEdit(Post post);
        void onPostDelete(Post post);
    }
}
