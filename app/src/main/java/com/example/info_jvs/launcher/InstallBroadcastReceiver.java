package com.example.info_jvs.launcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by info-jvs on 31-03-17.
 */

public class InstallBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(action.equals(Intent.ACTION_PACKAGE_ADDED)
                ||action.equals(Intent.ACTION_PACKAGE_INSTALL)){
            notifyServerForApplicationInstall(context, intent);

        }
    }
    private static final String TAG = "MyActivity";
    private void notifyServerForApplicationInstall(Context context, Intent intent){
        Log.v(TAG, "test");

    }
}