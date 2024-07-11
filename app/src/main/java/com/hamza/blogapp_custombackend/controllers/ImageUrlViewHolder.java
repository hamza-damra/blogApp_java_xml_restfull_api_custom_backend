package com.hamza.blogapp_custombackend.controllers;

import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;

public class ImageUrlViewHolder extends RecyclerView.ViewHolder {
    TextInputEditText etImageUrl;
    ImageButton btnRemove;

    public ImageUrlViewHolder(@NonNull View itemView) {
        super(itemView);
        etImageUrl = itemView.findViewById(R.id.et_image_url);
        btnRemove = itemView.findViewById(R.id.btn_remove);
    }

    public void bind(String url, ImageUrlAdapter.OnRemoveClickListener listener) {
        etImageUrl.setText(url);
        btnRemove.setOnClickListener(v -> listener.onRemoveClick(getAdapterPosition()));
    }
}