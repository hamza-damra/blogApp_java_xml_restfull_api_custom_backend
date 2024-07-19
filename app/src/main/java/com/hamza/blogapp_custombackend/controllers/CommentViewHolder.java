package com.hamza.blogapp_custombackend.controllers;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Comment;

public class CommentViewHolder extends RecyclerView.ViewHolder  {

    private MaterialCardView cardView;
    private final TextView tvUsername;
    private final TextView tvContent;


    public CommentViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.comment_card_view);
        tvUsername = itemView.findViewById(R.id.tv_name);
        tvContent = itemView.findViewById(R.id.tv_content);
    }

    public void bind(Comment comment) {
        tvUsername.setText(comment.getName());
        tvContent.setText(comment.getBody());
    }
}
