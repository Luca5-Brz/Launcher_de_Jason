package com.example.info_jvs.launcher;


import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by info-jvs on 29-08-17.
 */

  public class CheckRunningActivity extends Service {

    public String partsOfJsonString[] = {"", "", "", "", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public void onDestroy() {
        //Toast.makeText(getBaseContext(),"onDestroy", Toast.LENGTH_LONG).show();
        Log.e("TAG2", "onDestroy runningactivity");
        scheduleTaskExecutor.shutdown();
    }

    ScheduledExecutorService scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
    public void splitIt() {
        String reader=readFromFile(getApplicationContext());
        String[] arr = reader.split("[,;]");
        int i = 0;
        for (String s : arr) {
            // Log.d("Response: ", "> " + s);
            //  System.out.println(s);
            if (i == 0) {
                s = s.replaceAll("^\\s+", "");
            }
            partsOfJsonString[i] = s;
            i++;
        }

    }



    private String readFromFile(Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput("buttonConfig.txt");

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }



    public boolean isAllowed(String appName)
    {

        //parcourir l'arraylist à la recherche d'occurence et retourne vrai ou faux

        //utilisé par le service de vérification des taches en cours (checkRunningActivity class)
        boolean Occurence=false;
        for(int i = 0 ; i < partsOfJsonString.length ; i++)
        {
            String appInArray = partsOfJsonString[i];
            if (appName.equals(appInArray))
            {
                Occurence=true;
            }
          //  Log.e("appinArray"+i,appInArray);
        }


        return Occurence;

    }
    @Override
    public void onCreate() {

        super.onCreate();
        splitIt();

        final HomeActivity home=new HomeActivity();

       // Log.e("TAG","starting service CheckRunningActivity");
// This schedule a runnable task every 1 second
        scheduleTaskExecutor.scheduleAtFixedRate(new Runnable() {
            public void run() {
           int numberOfTasks = 1;
                ActivityManager m = (ActivityManager)getApplicationContext().getSystemService(ACTIVITY_SERVICE);
//Get some number of running tasks and grab the first one.  getRunningTasks returns newest to oldest
                ActivityManager.RunningTaskInfo task = m.getRunningTasks(numberOfTasks).get(0);


                List<ActivityManager.RunningTaskInfo> taskInfo = m.getRunningTasks(1);
               // Log.d("topActivity", "CURRENT Activity ::" + taskInfo.get(0).topActivity.getClassName());
                ComponentName componentInfo = taskInfo.get(0).topActivity;
                componentInfo.getPackageName();
//Build output
                String output  = task.baseActivity.toShortString();
                String output2[]=output.split("[{/]");
               // Log.e("TAG","starting service CheckRunningActivity2");

                boolean isallowed=isAllowed(output2[1]);
               // Log.e("TAG","starting service CheckRunningActivity2");

             // Log.e("TAG", output2[1]);

                //Log.e("Package name:==== ", "Package name     " + componentInfo.getPackageName());
                if ("com.computerland.cdh.mobile".equals(output2[1])) {}
                else if ("com.example.info_jvs.launcher".equals(output2[1])) { }
                else if ("com.android.settings".equals(output2[1])) { }
                else if ("com.teamviewer.quicksupport.market".equals(output2[1])){}
                else if ("com.android.packageinstaller".equals(output2[1])){}
                else if ("com.google.android.location".equals(output2[1])){}
                 else if (isallowed==true)
                {

                }
                else
                {
                    Log.e("TAG", output2[1] + "--> Refusé !");
                    Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.info_jvs.launcher");
                    startActivity(launchIntent);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);



    }


}