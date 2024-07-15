package com.hamza.blogapp_custombackend.controllers;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.hamza.blogapp_custombackend.R;

import java.util.List;

public class ImageUrlAdapter extends RecyclerView.Adapter<ImageUrlAdapter.ImageUrlViewHolder> {

    private final List<String> imageUrlList;
    private final OnRemoveClickListener onRemoveClickListener;

    public ImageUrlAdapter(List<String> imageUrlList, OnRemoveClickListener onRemoveClickListener) {
        this.imageUrlList = imageUrlList;
        this.onRemoveClickListener = onRemoveClickListener;
    }

    @NonNull
    @Override
    public ImageUrlViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_url, parent, false);
        return new ImageUrlViewHolder(view, new CustomTextWatcher());
    }

    @Override
    public void onBindViewHolder(@NonNull ImageUrlViewHolder holder, int position) {
        holder.bind(imageUrlList.get(position), position, onRemoveClickListener);
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < imageUrlList.size()) {
            imageUrlList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, imageUrlList.size());
        }
    }

    public static class ImageUrlViewHolder extends RecyclerView.ViewHolder {
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

        public void bind(String url, int position, OnRemoveClickListener listener) {
            textWatcher.updatePosition(position);
            etImageUrl.setText(url);
            btnRemove.setOnClickListener(v -> listener.onRemoveClick(getAdapterPosition()));
        }
    }

    private class CustomTextWatcher implements TextWatcher {
        private int position;

        void updatePosition(int position) {
            this.position = position;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            imageUrlList.set(position, s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
