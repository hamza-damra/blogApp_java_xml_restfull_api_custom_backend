package com.hamza.blogapp_custombackend.controllers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Post;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostViewHolder> {

    private List<Post> posts;
    private Context context;

    public PostAdapter(List<Post> posts, Context context) {
        this.posts = posts;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_custom_card_view, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post.getTitle(), post.getDescription(), post.getContent());
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }
}
