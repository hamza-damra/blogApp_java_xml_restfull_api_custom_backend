package com.hamza.blogapp_custombackend.controllers;

import android.view.View;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.utils.CustomTextWatcher;

public class ImageUrlViewHolder extends RecyclerView.ViewHolder {
    TextInputEditText etImageUrl;
    ImageButton btnRemove;
    CustomTextWatcher textWatcher;

    public ImageUrlViewHolder(@NonNull View itemView, CustomTextWatcher textWatcher) {
        super(itemView);
        etImageUrl = itemView.findViewById(R.id.et_image_url);
        btnRemove = itemView.findViewById(R.id.btn_remove);
        this.textWatcher = textWatcher;
        etImageUrl.addTextChangedListener(textWatcher);
    }

    public void bind(String url, int position, ImageUrlAdapter.OnRemoveClickListener listener) {
        textWatcher.updatePosition(position);
        etImageUrl.setText(url);
        btnRemove.setOnClickListener(v -> listener.onRemoveClick(getAdapterPosition()));
    }
}