package com.hamza.blogapp_custombackend.controllers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.hamza.blogapp_custombackend.R;
import com.hamza.blogapp_custombackend.utils.CustomTextWatcher;

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
        return new ImageUrlViewHolder(view, new CustomTextWatcher(imageUrlList));
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

    public interface OnRemoveClickListener {
        void onRemoveClick(int position);
    }
}
