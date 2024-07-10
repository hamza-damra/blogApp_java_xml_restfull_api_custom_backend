package com.hamza.blogapp_custombackend.controllers;

import android.app.AlertDialog;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.models.Post;

import java.util.Objects;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private MaterialCardView cardView;
    private LinearLayout layout;
    private TextView title;
    private TextView description;
    private TextView content;
    private PostAdapter.OnPostActionListener listener;

    public PostViewHolder(@NonNull View itemView, PostAdapter.OnPostActionListener listener) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view);
        layout = itemView.findViewById(R.id.layout);
        title = itemView.findViewById(R.id.tv_title);
        description = itemView.findViewById(R.id.tv_description);
        content = itemView.findViewById(R.id.tv_content);
        this.listener = listener;

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });

        cardView.setOnLongClickListener(v -> {
            showPostOptionDialog();
            return true;
        });

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
    }

    private void showPostOptionDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(itemView.getContext());
        dialogBuilder.setTitle("Post Options")
                .setItems(new String[]{"Edit", "Delete"}, (dialog, which) -> {
                    Post post = (Post) itemView.getTag();
                    switch (which) {
                        case 0:
                            listener.onPostEdit(post);
                            break;
                        case 1:
                            listener.onPostDelete(post);
                            break;
                    }
                });
        AlertDialog dialog = dialogBuilder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_background);
        }
        dialog.show();
    }

    public void bind(Post post) {
        this.title.setText(post.getTitle());
        this.description.setText(post.getDescription());
        this.content.setText(post.getContent());
        this.itemView.setTag(post);  // Set the post object as a tag on the itemView
    }

    private void expand() {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }

        TransitionManager.beginDelayedTransition(layout, new AutoTransition());
    }
}
