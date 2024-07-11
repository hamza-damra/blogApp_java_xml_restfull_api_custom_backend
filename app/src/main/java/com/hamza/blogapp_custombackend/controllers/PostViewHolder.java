package com.hamza.blogapp_custombackend.controllers;

import android.app.AlertDialog;
import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.fragments.CommentFragment;
import com.hamza.blogapp_custombackend.models.Post;

public class PostViewHolder extends RecyclerView.ViewHolder {
    private final LinearLayout layout;
    private final TextView title;
    private final TextView description;
    private final TextView content;
    private final ImageView arrowIcon;
    private final PostAdapter.OnPostActionListener listener;

    public PostViewHolder(@NonNull View itemView, PostAdapter.OnPostActionListener listener) {
        super(itemView);
        MaterialCardView cardView = itemView.findViewById(R.id.card_view);
        layout = itemView.findViewById(R.id.layout);
        title = itemView.findViewById(R.id.tv_title);
        description = itemView.findViewById(R.id.tv_description);
        content = itemView.findViewById(R.id.tv_content);
        arrowIcon = itemView.findViewById(R.id.goto_comment_screen_icon);
        this.listener = listener;

        cardView.setOnClickListener(v -> expand());

        cardView.setOnLongClickListener(v -> {
            showPostOptionDialog();
            return true;
        });

        arrowIcon.setOnClickListener(v -> navigateToCommentFragment());
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
        this.itemView.setTag(post);
    }

    private void expand() {
        if (content.getVisibility() == View.VISIBLE) {
            content.setVisibility(View.GONE);
        } else {
            content.setVisibility(View.VISIBLE);
        }

        TransitionManager.beginDelayedTransition(layout, new AutoTransition());
    }

    private void navigateToCommentFragment() {
        Post post = (Post) itemView.getTag();
        CommentFragment commentFragment = CommentFragment.newInstance(post);

        FragmentActivity activity = (FragmentActivity) itemView.getContext();
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, commentFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
