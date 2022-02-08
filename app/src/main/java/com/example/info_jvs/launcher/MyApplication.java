package com.example.info_jvs.launcher;

import android.app.Application;

/**
 * Created by info-jvs on 04-09-17.
 */

public class MyApplication extends Application {

    public static boolean isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }

    private static boolean activityVisible;
}