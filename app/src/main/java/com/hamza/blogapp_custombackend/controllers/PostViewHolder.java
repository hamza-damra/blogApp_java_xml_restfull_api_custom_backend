package com.hamza.blogapp_custombackend.controllers;

import android.transition.AutoTransition;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.hamza.blogapp_custombackend.R;

public class PostViewHolder extends RecyclerView.ViewHolder {
    MaterialCardView cardView;
    LinearLayout layout;
    TextView title;
    TextView description;
    TextView content;

    public PostViewHolder(@NonNull View itemView) {
        super(itemView);
        cardView = itemView.findViewById(R.id.card_view);
        layout = itemView.findViewById(R.id.layout);
        title = itemView.findViewById(R.id.tv_title);
        description = itemView.findViewById(R.id.tv_description);
        content = itemView.findViewById(R.id.tv_content);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });

        description.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                expand();
            }
        });
    }

    public void bind(String title, String description, String content) {
        this.title.setText(title);
        this.description.setText(description);
        this.content.setText(content);
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
