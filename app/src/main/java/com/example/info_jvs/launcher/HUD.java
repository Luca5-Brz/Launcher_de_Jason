package com.example.info_jvs.launcher;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import static android.R.attr.bitmap;

/**
 * Created by info-jvs on 23-08-17.
 */

public class HUD extends Service {
    View mView;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //Toast.makeText(this, "Service HUD Started", Toast.LENGTH_LONG).show();

        //Toast.makeText(getBaseContext(),"HUD Service starting", Toast.LENGTH_LONG).show();
        ////Log.e("TAG","HUD start");
        mView = new View(this);

        // Dessine une bande transparente au dessus de la Status Bar pour y bloquer l'accés
        WindowManager manager = ((WindowManager)   getApplicationContext().getSystemService(Context.WINDOW_SERVICE));
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        localLayoutParams.gravity = Gravity.TOP;
        localLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|

                // this is to enable the notification to recieve touch events
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE|

                // Draws over status bar
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        localLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        localLayoutParams.height = (int) (25 * getResources().getDisplayMetrics().scaledDensity);
        localLayoutParams.format = PixelFormat.TRANSPARENT;

        customViewGroup view = new customViewGroup(this);

        manager.addView(view, localLayoutParams);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        //Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show();
        if(mView != null)
        {
            ((WindowManager) getSystemService(WINDOW_SERVICE)).removeView(mView);
            mView = null;
        }
    }

    static class HUDView extends ViewGroup {
        private Paint mLoadPaint;
        ImageView drawingImageView;

        public HUDView(Context context) {
            super(context);
            //Toast.makeText(getContext(),"HUDView", Toast.LENGTH_LONG).show();

            mLoadPaint = new Paint();
            mLoadPaint.setAntiAlias(true);
            mLoadPaint.setTextSize(10);
            mLoadPaint.setARGB(255, 255, 0, 0);


        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
           // canvas.drawText("Hello World", 5, 15, mLoadPaint);



        }

        @Override
        protected void onLayout(boolean arg0, int arg1, int arg2, int arg3, int arg4) {
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {

            // ATTENTION: GET THE X,Y OF EVENT FROM THE PARAMETER
            // THEN CHECK IF THAT IS INSIDE YOUR DESIRED AREA


            Toast.makeText(getContext(),"onTouchEvent", Toast.LENGTH_LONG).show();
            return true;
        }



    }
}
