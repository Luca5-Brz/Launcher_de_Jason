package com.example.info_jvs.launcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

/**
 * Created by info-jvs on 23-08-17.
 */

public abstract class StatusBarService extends Service{
    WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
    View overlay;
    @Override
    public void onCreate() {
        super.onCreate();

        int statusBarHeight = (int) Math.ceil(25 * getResources().getDisplayMetrics().density);

        overlay.setBackgroundColor(Color.GREEN);
        overlay.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Log.i("StatusBar", "touched");
                return false;
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                statusBarHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
                        WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH|
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.TOP;

        windowManager.addView(overlay, params);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlay != null) windowManager.removeView(overlay);
    }
}
