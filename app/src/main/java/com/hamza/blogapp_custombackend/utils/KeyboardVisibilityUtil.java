package com.hamza.blogapp_custombackend.utils;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewTreeObserver;

public class KeyboardVisibilityUtil {

    public interface KeyboardVisibilityListener {
        void onKeyboardVisibilityChanged(boolean isVisible);
    }

    public static void setKeyboardVisibilityListener(View rootLayout, final KeyboardVisibilityListener listener) {
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean wasVisible = false;

            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootLayout.getRootView().getHeight();
                int keypadHeight = screenHeight - r.bottom;
                boolean isVisible = keypadHeight > screenHeight * 0.15;
                if (isVisible != wasVisible) {
                    listener.onKeyboardVisibilityChanged(isVisible);
                    wasVisible = isVisible;
                }
            }
        });
    }
}
