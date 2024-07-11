package com.hamza.blogapp_custombackend.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamza.blogapp_custombackend.R;
import java.util.List;

public class ImageUrlAdapter extends RecyclerView.Adapter<ImageUrlViewHolder> {
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
        return new ImageUrlViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageUrlViewHolder holder, int position) {
        String imageUrl = imageUrlList.get(position);
        holder.etImageUrl.setText(imageUrl);

        holder.btnRemove.setOnClickListener(v -> onRemoveClickListener.onRemoveClick(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return imageUrlList.size();
    }

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
