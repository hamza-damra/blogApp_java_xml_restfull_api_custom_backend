package com.hamza.blogapp_custombackend.utils;

import android.text.Editable;
import android.text.TextWatcher;

import java.util.List;

public class CustomTextWatcher implements TextWatcher {
    private int position;
    private final List<String> imageUrlList;

    public CustomTextWatcher(List<String> imageUrlList) {
        this.imageUrlList = imageUrlList;
    }

    public void updatePosition(int position) {
        this.position = position;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // imageUrlList.set(position, s.toString());
    }

    @Override
    public void afterTextChanged(Editable s) {
        imageUrlList.set(position, s.toString());
    }
}