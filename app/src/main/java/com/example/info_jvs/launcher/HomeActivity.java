package com.example.info_jvs.launcher;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.app.WallpaperManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import android.os.Handler;


public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

     public static final String MyPREFERENCES = "MyPrefs";
    public static final String IPTitle = "nameKey";
    public static final String Overlay = "overlay";
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;
    private static final int MY_PERMISSIONS_REQUEST_STORAGE = 0;
    private static final int MY_PERMISSIONS_REQUEST_PHONE_STATES = 0;
    private static final int MY_PERMISSIONS_REQUEST_OVERLAY = 0;
    private static final int MY_PERMISSIONS_REQUEST_WRITE_SETTINGS = 0;
    private static String urlString;
    //Disable the volume buttons:
    private final List blockedKeys = new ArrayList(Arrays.asList(KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_VOLUME_UP));
    public String partsOfJsonString[] = {"com.computerland.cdh.mobile", "", "", "com.teamviewer.quicksupport.market", "", "", "", "", "","", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""};
    Boolean phoneStates=false;
    String IPfull = "";
    String StoreApp = "";
    String JsonString = "";
    String storeUrl="http://212.166.21.236:8080/Store/";
    String myUrl = "http://212.166.21.236:8080/StoreRequest.php";
    String IP = "";
    boolean downloading = true;
    Boolean IsServerAvailable=false;
    int brightnessmode;
    String apkName=null;
    String VersionToUninstall=null;
    ProgressDialog pd;
    private DownloadManager downloadManager;
    private long myDownloadReference;
    SharedPreferences sharedpreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        settingPermissionGPS();
        settingPermission();


        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1) {
                setContentView(R.layout.activity_home);

                ///if ok --> start all services
                String serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.e(" TAG","Serial: "+serialNumber);
                //gère l'orientation pour les tablettes/gun
                setOrientation();

                int time;
                time = 1800000;

                sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedpreferences.edit();
                String getPref = sharedpreferences.getString(IPTitle, null);
                android.provider.Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, time);
                if (getPref==null)
                {
                    //récupération de l'adresse ip du gun
                    IPfull = Utils.getIPAddress(true);
                    //récupération du dernier octet de l'adresse ip
                    String[] parts = IPfull.split("\\."); // escape .
                    IP = parts[3];
                    //ajout du dernier octet dans une shared préférence
                    editor.putString(IPTitle, IP);
                    editor.commit();

                    //débug --> me prévient si cette condition s'éxécute
                    Toast.makeText(this,"Création de la sharedPreference",Toast.LENGTH_LONG).show();
                    this.setTitle(parts[3]);
                    new JsonTask().execute(myUrl);
                    // checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                }
                else
                {

                    //affichage du dernier octet de l'adresse ip sur l'écran
                    this.setTitle(getPref);

                    //job de récupération des boutons de l'écran
                    new JsonTask().execute(myUrl);

                }
                //checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                setSeekbar();

                startServiceHUD();

                startService(new Intent(this, GetPositionService.class));
                startService(new Intent(this, GetNewMessageService.class));
                splitIt();


        }




    }

    private void  setOrientation()
    {
        if ((getResources().getConfiguration().screenLayout &Configuration.SCREENLAYOUT_SIZE_MASK) ==4)
        {
            // a large screen device ...
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }
        else
        {   //a small one
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }
    private void startServiceHUD()
    {
        //démarrer le service du bloquage de la status bar
        int currentAPIVersion = android.os.Build.VERSION.SDK_INT; //Check la version actuelle d'Android

        if (currentAPIVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) //Si Android est plus petit ou = à 4.2.2
        {
            startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 4.2.2
        }
        /*else if (currentAPIVersion <= android.os.Build.VERSION_CODES.LOLLIPOP_MR1)
        {

             startService(new Intent(HomeActivity.this, HUD.class));// android 5.1.1
        }*/
        else
        {
            //Toast.makeText(this,"tablette",Toast.LENGTH_LONG).show();

            startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 5.1.2 et plus

        }
    }

    public void checkUrlAcessiblilityBeforeExecute(String url)
    {
    final String customURL = url;
   // Toast.makeText(this,customURL,Toast.LENGTH_LONG).show();
    new Thread() {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            super.run();
            try {
                URL url = new URL(customURL);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("HEAD");
                con.connect();
                Log.i("TAG", "con.getResponseCode() IS : " + con.getResponseCode());
                if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    IsServerAvailable=true;
                    Log.i("TAG", "Sucess check header "+IsServerAvailable.toString());

                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("TAG", "fail check header");

            }
        }

    }.start();
    startJsonTask();
   // IsServerAvailable=false;
}

    public void startJsonTask ()
    {
        if (IsServerAvailable.equals(true)) {
            new JsonTask().execute(myUrl);
            splitIt();
            Toast.makeText(this,"no problem",Toast.LENGTH_LONG).show();
        }
        else
        {
            //load button from TXT files
            splitIt();
            Toast.makeText(this,"problem",Toast.LENGTH_LONG).show();
        }
    }

    private void settingPermissionGPS()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED)
            {

                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.ACCESS_FINE_LOCATION))
                {

                } else
                {
                    Toast.makeText(this,"ask GPS set",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
                }
            }
            else
            {
                // Permission has already been granted
               // Toast.makeText(this,"already GPS set",Toast.LENGTH_LONG).show();
                settingPermissionStorage();
            }
        }
    }

    private void settingPermissionStorage()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE))
                {

                }
                else
                {
                    Toast.makeText(this,"ask storage set",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_STORAGE);

                }
            } else
            {
                // Permission has already been granted
               // Toast.makeText(this,"already storage set",Toast.LENGTH_LONG).show();
                settingPermissionPhoneStates();
            }
        }
    }

    private void settingPermissionPhoneStates()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED)
            {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_PHONE_STATE))
                {

                }
                else
                {
                   // Toast.makeText(this,"ask phone state set",Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_PHONE_STATE},MY_PERMISSIONS_REQUEST_PHONE_STATES);
                }
            } else
            {
                // Permission has already been granted
                //Toast.makeText(this,"already phone state set",Toast.LENGTH_LONG).show();
                phoneStates=true;
                settingPermissionOverlay();
            }
        }
    }

    //ne le démarrer que si toutes les permission sont activées
    private void settingPermissionOverlay()
    {
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        Boolean overlay = sharedpreferences.getBoolean(Overlay,false);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                //Toast.makeText(this,"settings overlay",Toast.LENGTH_LONG).show();
                if (overlay==false)
                {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, MY_PERMISSIONS_REQUEST_OVERLAY);
                    editor.putBoolean(Overlay,true);
                    editor.commit();
                }
                else
                {
                    String getPref = sharedpreferences.getString(IPTitle, null);
                    ///if ok --> start the app entirely
                    String serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    Log.e(" TAG",serialNumber);
                    //gère l'orientation pour les tablettes/gun
                    if ((getResources().getConfiguration().screenLayout &Configuration.SCREENLAYOUT_SIZE_MASK) ==4)
                    {
                        // a large screen device ...
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                    }
                    else
                    {
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }

                    // Toast.makeText(this, "homeAct create", Toast.LENGTH_LONG).show();

                    setContentView(R.layout.activity_home);
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                    Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                    TableLayout table = (TableLayout) findViewById(R.id.table);
                    //table.setBackground(wallpaperDrawable);
                    int time;
                    time = 1800000;

//                    android.provider.Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, time);
                    if (getPref==null)
                    {
                        //récupération de l'adresse ip du gun
                        IPfull = Utils.getIPAddress(true);
                        //récupération du dernier octet de l'adresse ip
                        String[] parts = IPfull.split("\\."); // escape .
                        IP = parts[3];
                        //ajout du dernier octet dans une shared préférence
                        editor.putString(IPTitle, IP);
                        editor.commit();

                        //débug --> me prévient si cette condition s'éxécute
                        Toast.makeText(this,"Création de la sharedPreference",Toast.LENGTH_LONG).show();
                        this.setTitle(parts[3]);
                        new JsonTask().execute(myUrl);
                        // checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                    }
                    else
                    {

                        //affichage du dernier octet de l'adresse ip sur l'écran
                        this.setTitle(getPref);

                        //job de récupération des boutons de l'écran
                        new JsonTask().execute(myUrl);

                    }
                    //checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                    setSeekbar();

                    //démarrer le service du bloquage de la status bar
                    int currentAPIVersion = android.os.Build.VERSION.SDK_INT;

                    if (currentAPIVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
                        startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 4.2.2
                    }
                    else if (currentAPIVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
                        startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 5
                        // Toast.makeText(this,"lolly",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        //Toast.makeText(this,"tablette",Toast.LENGTH_LONG).show();

                        startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 5

                    }

                    startService(new Intent(this, GetPositionService.class));
                    startService(new Intent(this, GetNewMessageService.class));
                    splitIt();



                }
            }
            else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
            {
                String getPref = sharedpreferences.getString(IPTitle, null);

                ///if ok --> start the app entirely
                String serialNumber = Build.SERIAL != Build.UNKNOWN ? Build.SERIAL : Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                Log.e(" TAG",serialNumber);
                //gère l'orientation pour les tablettes/gun
                if ((getResources().getConfiguration().screenLayout &Configuration.SCREENLAYOUT_SIZE_MASK) ==4)
                {
                    // a large screen device ...
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                }
                else
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }

                // Toast.makeText(this, "homeAct create", Toast.LENGTH_LONG).show();

                setContentView(R.layout.activity_home);
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
                Drawable wallpaperDrawable = wallpaperManager.getDrawable();
                TableLayout table = (TableLayout) findViewById(R.id.table);
                //table.setBackground(wallpaperDrawable);
                int time;
                time = 1800000;

                android.provider.Settings.System.putInt(getContentResolver(),Settings.System.SCREEN_OFF_TIMEOUT, time);
                if (getPref==null)
                {
                    //récupération de l'adresse ip du gun
                    IPfull = Utils.getIPAddress(true);
                    //récupération du dernier octet de l'adresse ip
                    String[] parts = IPfull.split("\\."); // escape .
                    IP = parts[3];
                    //ajout du dernier octet dans une shared préférence
                    editor.putString(IPTitle, IP);
                    editor.commit();

                    //débug --> me prévient si cette condition s'éxécute
                    Toast.makeText(this,"Création de la sharedPreference",Toast.LENGTH_LONG).show();
                    this.setTitle(parts[3]);
                    new JsonTask().execute(myUrl);
                    // checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                }
                else
                {

                    //affichage du dernier octet de l'adresse ip sur l'écran
                    this.setTitle(getPref);

                    //job de récupération des boutons de l'écran
                    new JsonTask().execute(myUrl);

                }
                //checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");

                setSeekbar();

                //démarrer le service du bloquage de la status bar
                int currentAPIVersion = android.os.Build.VERSION.SDK_INT;

                if (currentAPIVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
                    startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 4.2.2
                }
                else if (currentAPIVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
                    startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 5
                    // Toast.makeText(this,"lolly",Toast.LENGTH_LONG).show();
                }
                else
                {
                    //Toast.makeText(this,"tablette",Toast.LENGTH_LONG).show();

                    startService(new Intent(HomeActivity.this, HUD.class));//fonctionne avec android 5

                }

                startService(new Intent(this, GetPositionService.class));
                startService(new Intent(this, GetNewMessageService.class));
                splitIt();



            }

    }

    private void settingPermissionWriteSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            //Toast.makeText(this,"setting write settings",Toast.LENGTH_LONG).show();

            if (!Settings.System.canWrite(getApplicationContext())) {
                Intent intent2 = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent2, 200);
                // ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_SETTINGS},MY_PERMISSIONS_REQUEST_WRITE_SETTINGS);
            }
            else
            {
                settingPermissionGPS();
            }

        }
    }

    private void settingPermission()
    {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {

        }
        if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            // Do something for lollipop and above versions
            //
           // Toast.makeText(this,"settingPermission",Toast.LENGTH_LONG).show();
           // Toast.makeText(this,"lollipop",Toast.LENGTH_LONG).show();
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.ACCESS_FINE_LOCATION)) {

                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 0);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, 0);
                }
            } else {
                //Toast.makeText(this, "" +  Manifest.permission.ACCESS_FINE_LOCATION + " is already granted.", Toast.LENGTH_SHORT).show();
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.READ_PHONE_STATE)) {

                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 0);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_PHONE_STATE}, 0);
                }
            } else {
              // Toast.makeText(this, "" +  Manifest.permission.READ_PHONE_STATE + " is already granted.", Toast.LENGTH_SHORT).show();
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.READ_EXTERNAL_STORAGE)) {

                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE}, 0);

                }
            } else {
               // Toast.makeText(this, "" +  Manifest.permission.READ_EXTERNAL_STORAGE + " is already granted.", Toast.LENGTH_SHORT).show();
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);

                }
            } else {
               // Toast.makeText(this, "" +  Manifest.permission.WRITE_EXTERNAL_STORAGE + " is already granted.", Toast.LENGTH_SHORT).show();
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,  Manifest.permission.ACCESS_COARSE_LOCATION)) {

                    //This is called if user has denied the permission before
                    //In this case I am just asking the permission again
                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

                } else {

                    ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION}, 0);

                }
            } else {
               // Toast.makeText(this, "" +  Manifest.permission.ACCESS_COARSE_LOCATION + " is already granted.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String permissions[], int[] grantResults)
    {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

           /* if (requestCode==MY_PERMISSIONS_REQUEST_WRITE_SETTINGS) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Toast.makeText(this,"settings",Toast.LENGTH_LONG).show();
                }
                else
                {
                   //retry
                }

            }*/
            if (requestCode==MY_PERMISSIONS_REQUEST_LOCATION) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this,"if request location",Toast.LENGTH_LONG).show();

                    settingPermissionStorage();

                }
                else
                {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }

            }

            if (MY_PERMISSIONS_REQUEST_STORAGE==1) {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0&& grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(this,"if request storage",Toast.LENGTH_LONG).show();
                    settingPermissionPhoneStates();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

            if (MY_PERMISSIONS_REQUEST_PHONE_STATES==1) {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                   // Toast.makeText(this,"if request phone states",Toast.LENGTH_LONG).show();
                     phoneStates=true;

                    settingPermissionOverlay();
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
            }

        }

    @Override
    public void onRestart()
    {
        super.onRestart();

       // setSeekbar();
       // Toast.makeText(this,"onrestart",Toast.LENGTH_LONG).show();
    }


    @Override
    public void onStart()
    {
        super.onStart();
        //setSeekbar();
        //Toast.makeText(this,"onstart",Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        //si android M
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
        {
            settingPermissionWriteSettings();
        }
        else
        {

            setSeekbar();
            initializeButton();
        //vérification de l'état du réseau après un sortie de veille
            verifyNetwork();
        //rafraichis le "bureau" --> télécharge la config pour les boutons après avoir vérifié la connection au serveur
        //recharge les boutons
            initializeButton();
       MyApplication.activityResumed();
        }
    }

    //téléchargement des app non installées (n'est pas utilisé pour les update !! )
    public void download(final String filename) {

        String path="";
        int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
        if (currentAPIVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
            // Do something for JellyBean 4.2.2
           // path = Environment.getDataDirectory()+"/com.example.info_jvs.launcher/files/Download";
            path = Environment.getExternalStorageDirectory() + "/Android/data/com.example.info_jvs.launcher/files/Download/";
            //Toast.makeText(this, Environment.getExternalStorageDirectory() +"/Android/data/com.example.info_jvs.launcher/files/Download",Toast.LENGTH_LONG ).show();
        }

        else if (currentAPIVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
            // Do something for JellyBean 5.1
            path = "/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download";


        }
        else{
            // do something for phones running an SDK above JellyBean
            path = Environment.getExternalStorageDirectory() + "/files/Download/";
            
        }

       // Si le fichiers existe déja, on le supprime
        File f = new File(path+"/"+filename);
        if (f.exists())
        {
            f.delete();
        }

        //initialisation du gestionnaire de téléchargement
        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(storeUrl+filename);
        final DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, filename);
        myDownloadReference = downloadManager.enqueue(request);

        //////////////////
        ///progressBar///
        ////////////////

        final ProgressDialog progressBarDialog = new ProgressDialog(this);
        progressBarDialog.setTitle("Téléchargement en cours ...");
        progressBarDialog.setMessage("À la fin du téléchargement, appuyez sur installer");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setCancelable(false);

        progressBarDialog.setProgress(0);
        //création d'un thead différent pour le téléchargemet (obligatoir)
        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                //tant que cela télécharge, on boucle
                while (downloading)
                {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(myDownloadReference); //filter by id which you have receieved when reqesting download from download manager
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                    //permet de sortir de la boucle quand le téléchargement est fini
                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);
                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            //affiche où en est le téléchargement
                            progressBarDialog.setProgress((int) dl_progress);

                        }
                    });

                    cursor.close();
                }

            }
        }).start();


        //show the dialog
        progressBarDialog.show();
        //////////////////////
        //Fin progressBar////
        ////////////////////


        /////////////////////////////////////////////////////////////
        // "surveille" la fin du téléchargement avant de l'éxécuter///
        ///////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {

                final long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    //attendre 1 seconde avant d'éxécuter
                    android.os.Handler handler = new android.os.Handler();
                    handler.postDelayed(new Runnable() {
                        public void run()
                        {
                            //executer le fichier
                            String dir="";
                            int currentAPIVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentAPIVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
                                // Do something for JellyBean and above versions
                                dir = Environment.getExternalStorageDirectory() + "/Android/data/com.example.info_jvs.launcher/files/Download/";

                            }
                            else if (currentAPIVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
                                // Do something for JellyBean and above versions
                                dir = "/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download";


                            }
                            else{
                                // do something for phones running an SDK above JellyBean
                                dir = Environment.getExternalStorageDirectory() + "/files/Download/";
                            }
                             File file = new File(dir, filename);
                            Intent promt = new Intent(Intent.ACTION_VIEW);
                            promt.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            promt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            promt.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                            startActivity(promt);
                            downloading = false;
                            progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Check();
                                    initializeButton();
                                }
                            });
                            progressBarDialog.hide();
                            progressBar();

                            // closeapp();
                        }
                    }, 600);
                }

            }
        };
        registerReceiver(receiver, filter);
    }

    private void progressBar()
    {
        //crée la progressBar de téléchargement
        final ProgressDialog progressBarDialog2= new ProgressDialog(this);
        progressBarDialog2.setTitle("Téléchargement terminé");
        progressBarDialog2.setMessage("Appuyez sur Fermer");
        progressBarDialog2.setCancelable(false);
        progressBarDialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
               // Check();
                initializeButton();
            }
        });
        progressBarDialog2.show();

    }
    private void verifyNetwork()
    {

     ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
     NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

     NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

     //si la tablette est en 4g/3g --> passage temporaire en wifi
     if (mData4G != null && mData4G.isConnected()) {
         myUrl="http://212.166.21.236:8080/StoreRequest.php";
         //Toast.makeText(this,urlString, Toast.LENGTH_SHORT).show();
         // checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");
         new JsonTask().equals(myUrl);
     }
     else
     {
         WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
         wManager.setWifiEnabled(true);

         String android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(),
                 Settings.Secure.ANDROID_ID);
         myUrl="http://192.168.1.211/StoreRequest.php?deviceID="+android_id;
         Log.i("myURL",myUrl);
         // checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");
         new JsonTask().equals(myUrl);
     }
 }
    @Override
    public void onBackPressed() {

        String deviceId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);
        //Toast.makeText(this,deviceId,Toast.LENGTH_LONG).show();

        //vérification de l'état du réseau
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if (mData4G != null && mData4G.isConnected()) {
            myUrl="http://212.166.21.236:8080/StoreRequest.php";
            //Toast.makeText(this,urlString, Toast.LENGTH_SHORT).show();
            //allumer la 4g ??
        }
        else
        {
            WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            wManager.setWifiEnabled(true);
            myUrl="http://192.168.1.211/StoreRequest.php";
        }
        splitIt();
        initializeButton();

        //Intent intent = new Intent(this, GPSLocation.class);
        //startActivity(intent);

       /* AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                your background code
            }
        });*/
        return;


    }

    @Override
    protected void onPause() {
        super.onPause();
      }

    //bare de changement de luminosité
    private void setSeekbar() {

        try {
            brightnessmode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            Log.d("tag", e.toString());
        }

        SeekBar seekBar = (SeekBar) findViewById(R.id.seekBar1);
        TextView textViewLuminosity = (TextView) findViewById(R.id.textViewLuminosity);
       seekBar.setVisibility(View.VISIBLE);
        textViewLuminosity.setVisibility(View.VISIBLE);
        if (brightnessmode == 0) {
            // luminosité auto off

        seekBar.setMax(255);
        float curBrightnessValue = 0;

        //essaye d'accèder à la luminosité actuelle
        try
        {
            curBrightnessValue = android.provider.Settings.System.getInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS);
        }
        catch (Settings.SettingNotFoundException e)
        {
            e.printStackTrace();
        }

        int screen_brightness = (int) curBrightnessValue;
        seekBar.setProgress(screen_brightness);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progresValue,boolean fromUser)
            {
                progress = progresValue;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
                android.provider.Settings.System.putInt(getContentResolver(),android.provider.Settings.System.SCREEN_BRIGHTNESS,progress);
            }
        });

        }

        if (brightnessmode == 1) {
            //luminosité auto on
            seekBar.setVisibility(View.GONE);
            textViewLuminosity.setVisibility(View.GONE);
        }
    }

    private void writeToFile(String data,Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput("buttonConfig.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
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

    //initialise tout les boutons (12 boutons maximum)
    public void initializeButton() {

        //récupération de l'élément visuel
        Button mClickButton1 = (Button) findViewById(R.id.button);
        //"surveiller" les action sur ce bouton
        mClickButton1.setOnClickListener(this);
        //récupération de l'application à afficher dans l'arraylist
        String packageName = partsOfJsonString[0].toString();//ici array [0] à la place de "com. ......"

        if (packageName.isEmpty())
        {}
        else {
            //opacité de l'image dans le bouton à 100%
           mClickButton1.getBackground().setAlpha(255);


           try {
               //on affiche l'icone de l'application sur le bouton
               Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
               PackageManager packageManager = getApplicationContext().getPackageManager();
               String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
               //on affiche le nom de l'application
               mClickButton1.setText(appName);
               mClickButton1.setBackgroundDrawable(appIcon);



               mClickButton1.setVisibility(View.VISIBLE);
           } catch (PackageManager.NameNotFoundException e) {
               e.printStackTrace();
               mClickButton1.setVisibility(View.VISIBLE);
           }
        }
           packageName = null;


        Button mClickButton2 = (Button) findViewById(R.id.button2);
        mClickButton2.setOnClickListener(this);
        packageName = partsOfJsonString[3].toString();//ici array [3] à la place de "com. ......"
        if (packageName.isEmpty()){}
        else {
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = "";
                appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton2.setText(appName);

                //getupdate(packageName);
                mClickButton2.setBackgroundDrawable(appIcon);
                mClickButton2.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();

                mClickButton2.setVisibility(View.VISIBLE);

            }
        }
        packageName = null;
        apkName="";

        Button mClickButton3 = (Button) findViewById(R.id.button3);
        mClickButton3.setOnClickListener(this);
        packageName = partsOfJsonString[6].toString();//ici array [3] à la place de "com. ......"
        apkName=partsOfJsonString[8].toString();
         if (packageName.isEmpty()){}
        else {
            mClickButton3.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton3.setText(appName);
                if (mClickButton3.getText() == "") {
                    mClickButton3.setVisibility(View.INVISIBLE);
                }
               mClickButton3.setBackgroundDrawable(appIcon);
                mClickButton3.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton3.setVisibility(View.VISIBLE);

            }
        }
        packageName = null;
        apkName="";

        Button mClickButton4 = (Button) findViewById(R.id.button4);
        mClickButton4.setOnClickListener(this);
        packageName = partsOfJsonString[9].toString();
        //Toast.makeText(this,packageName,Toast.LENGTH_LONG).show();
        apkName=partsOfJsonString[11].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton4.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton4.setText(appName);
                if (mClickButton4.getText() == "") {
                    mClickButton4.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton4.setBackgroundDrawable(appIcon);
                mClickButton4.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton4.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton5 = (Button) findViewById(R.id.button5);
        mClickButton5.setOnClickListener(this);
        packageName = partsOfJsonString[12].toString();
        apkName=partsOfJsonString[14].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton5.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton5.setText(appName);
                if (mClickButton5.getText() == "") {
                    mClickButton5.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton5.setBackgroundDrawable(appIcon);
                mClickButton5.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton5.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton6 = (Button) findViewById(R.id.button6);
        mClickButton6.setOnClickListener(this);

        packageName = partsOfJsonString[15].toString();
        apkName=partsOfJsonString[17].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton6.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton6.setText(appName);
                if (mClickButton6.getText() == "") {
                    mClickButton6.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton6.setBackgroundDrawable(appIcon);
                mClickButton6.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton6.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton7 = (Button) findViewById(R.id.button7);
        mClickButton7.setOnClickListener(this);
        packageName = partsOfJsonString[18].toString();
        apkName=partsOfJsonString[20].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton7.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton7.setText(appName);
                if (mClickButton7.getText() == "") {
                    mClickButton7.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton7.setBackgroundDrawable(appIcon);
                mClickButton7.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton7.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton8 = (Button) findViewById(R.id.button8);
        mClickButton8.setOnClickListener(this);

        packageName = partsOfJsonString[21].toString();
        apkName=partsOfJsonString[23].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton8.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton8.setText(appName);
                if (mClickButton8.getText() == "") {
                    mClickButton8.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton8.setBackgroundDrawable(appIcon);
                mClickButton8.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton8.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton9 = (Button) findViewById(R.id.button9);
        mClickButton9.setOnClickListener(this);

        packageName = partsOfJsonString[24].toString();
        apkName=partsOfJsonString[26].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton9.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton9.setText(appName);
                if (mClickButton9.getText() == "") {
                    mClickButton9.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton9.setBackgroundDrawable(appIcon);
                mClickButton9.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton5.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton10 = (Button) findViewById(R.id.button10);
        mClickButton10.setOnClickListener(this);

        packageName = partsOfJsonString[27].toString();
        apkName=partsOfJsonString[29].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton10.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton10.setText(appName);
                if (mClickButton10.getText() == "") {
                    mClickButton5.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton10.setBackgroundDrawable(appIcon);
                mClickButton10.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton10.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton11 = (Button) findViewById(R.id.button11);
        mClickButton11.setOnClickListener(this);
        packageName = partsOfJsonString[30].toString();
        apkName=partsOfJsonString[32].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton11.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton11.setText(appName);
                if (mClickButton11.getText() == "") {
                    mClickButton11.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton11.setBackgroundDrawable(appIcon);
                mClickButton11.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton11.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton12 = (Button) findViewById(R.id.button12);
        mClickButton12.setOnClickListener(this);
        packageName = partsOfJsonString[33].toString();
        apkName=partsOfJsonString[35].toString();
        if (packageName.isEmpty()){}
        else {
            mClickButton12.getBackground().setAlpha(255);
            try {
                Drawable appIcon = getPackageManager().getApplicationIcon(packageName);
                PackageManager packageManager = getApplicationContext().getPackageManager();
                String appName = (String) packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA));
                mClickButton12.setText(appName);
                if (mClickButton12.getText() == "") {
                    mClickButton12.setVisibility(View.INVISIBLE);
                }
                //getupdate(packageName);
                mClickButton12.setBackgroundDrawable(appIcon);
                mClickButton12.setVisibility(View.VISIBLE);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                mClickButton12.setVisibility(View.VISIBLE);
            }
        }
        packageName = null;
        apkName="";

        Button mClickButton13 = (Button) findViewById(R.id.button13);
        mClickButton13.setOnClickListener(this);

        Button mClickButton14 = (Button) findViewById(R.id.button14);
        mClickButton14.setOnClickListener(this);

        Button mClickButton15 = (Button) findViewById(R.id.button15);
        mClickButton15.setOnClickListener(this);

        Button mClickButton16 = (Button) findViewById(R.id.tools);
        mClickButton16.setOnClickListener(this);

    }

    //Blocage des boutons de Volume
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (blockedKeys.contains(event.getKeyCode())) {
            return true;
        } else {
            return super.dispatchKeyEvent(event);
        }
    }

     public void onClick(View v) {
         ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

         NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

//vérifie si c'est pas une tablette ou pas (plantage car pas de 4g sur tablette)
         if(getResources().getBoolean(R.bool.isTab))
         {
            // Toast.makeText(this,"Tablette",Toast.LENGTH_LONG).show();

             if (mData4G != null && mData4G.isConnected()) {

                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(false);
                // Toast.makeText(this,"ICI 4",Toast.LENGTH_LONG).show();

                 myUrl="http://212.166.21.236:8080/StoreRequest.php";

                 //  Toast.makeText(this,"4g",Toast.LENGTH_LONG).show();
             }
             else if (mData4G == null && mWifi == null)
             {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(true);
                 // Toast.makeText(this,"only wifi",Toast.LENGTH_LONG).show();
                 myUrl="http://192.168.1.211/StoreRequest.php";
             }

             else
             {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                myUrl="http://192.168.1.211/StoreRequest.php";
             }

         }
         else
         {
            // Toast.makeText(this,"mobile",Toast.LENGTH_LONG).show();
             if (mData4G != null && mData4G.isConnected()) {

                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(false);
                // Toast.makeText(this,"ICI 1",Toast.LENGTH_LONG).show();

                 myUrl="http://212.166.21.236:8080/StoreRequest.php";
             }
             else if (mData4G == null && mWifi == null)
             {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(true);

                 myUrl="http://192.168.1.211/StoreRequest.php";
             }
            else if(mData4G.isConnectedOrConnecting())
            {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(false);
               // Toast.makeText(this,"ICI 3",Toast.LENGTH_LONG).show();

                myUrl="http://212.166.21.236:8080/StoreRequest.php";
            }
             else
             {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 myUrl="http://192.168.1.211/StoreRequest.php";
             }

             if (mData4G != null && mData4G.isConnected())
             {
                 WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                 wManager.setWifiEnabled(false);
                 //Toast.makeText(this,"ICI5",Toast.LENGTH_LONG).show();

             }
             else
             {

             }


         }

//lancement des applications
        switch (v.getId()) {
            case R.id.button: {

                    Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                    Bundle b = new Bundle();
                    b.putString("packageName", partsOfJsonString[0]);
                    b.putString("apkName", partsOfJsonString[2]); //Your id
                    b.putString("valueOfUninstall",VersionToUninstall);
                    intent.putExtras(b); //Put your id to your next Intent
                    startActivity(intent);



                break;
            }

            case R.id.button2: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[3]);
                b.putString("apkName", partsOfJsonString[5]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button3: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[6]);
                b.putString("apkName", partsOfJsonString[8]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button4: {
               /* Intent launchIntent = getPackageManager().getLaunchIntentForPackage(partsOfJsonString[9].toString());//arraylist pour le nom de l'app?
                if (launchIntent != null) {
                    startActivity(launchIntent);//null pointer check in case package name was not found
                }//si cela n'existe pas --> download it
                else
                {
                    download(partsOfJsonString[11].toString());
                }
                break;*/

                //Toast.makeText(this, partsOfJsonString[9], Toast.LENGTH_LONG).show();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[9]);
                b.putString("apkName", partsOfJsonString[11]); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);

                break;
            }

            case R.id.button5: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[12]);
                b.putString("apkName", partsOfJsonString[14]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button6: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[15]);
                b.putString("apkName", partsOfJsonString[17]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button7: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[18]);
                b.putString("apkName", partsOfJsonString[20]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button8: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[21]);
                b.putString("apkName", partsOfJsonString[23]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button9: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[24]);
                b.putString("apkName", partsOfJsonString[26]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button10: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[27]);
                b.putString("apkName", partsOfJsonString[29]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button11: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[30]);
                b.putString("apkName", partsOfJsonString[32]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }

            case R.id.button12: {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                Bundle b = new Bundle();
                b.putString("packageName", partsOfJsonString[33]);
                b.putString("apkName", partsOfJsonString[35]); //Your id
                b.putString("valueOfUninstall",VersionToUninstall);
                intent.putExtras(b); //Put your id to your next Intent
                startActivity(intent);



                break;
            }



            case R.id.button13: {
                Toast.makeText(this, "Killed", Toast.LENGTH_LONG).show();
                ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
                am.killBackgroundProcesses("com.computerland.cdh.mobile");
                break;
            }

            case R.id.button14: {
                // get prompts_password.xml view
                LayoutInflater li = LayoutInflater.from(this);
                View promptsView = li.inflate(R.layout.prompt_password, null);

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

                // set prompts.xml to alertdialog builder
                alertDialogBuilder.setView(promptsView);

                final EditText userInput = (EditText) promptsView
                        .findViewById(R.id.editTextDialogUserInput);

                // set dialog message
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // get user input and set it to result
                                        // edit text
                                        Date c = Calendar.getInstance().getTime();
                                        System.out.println("Current time => " + c);

                                        SimpleDateFormat df = new SimpleDateFormat("yyMMdd");
                                        String formattedDate = df.format(c);
                                       // Toast.makeText(HomeActivity.this,formattedDate,Toast.LENGTH_LONG).show();
                                        ///mot de passe dans le code pour ne pas avoir à configurer cela au démarrage de l'application.
                                        if (userInput.getText().toString().equals(formattedDate)) {
                                            // getApplicationContext().stopService(new Intent(getApplicationContext(),CheckRunningActivity.class));
                                            new Handler().postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                   // getApplicationContext().stopService(new Intent(getApplicationContext(),CheckRunningActivity.class));
                                                   // getApplicationContext().stopService(new Intent(getApplicationContext(),HUD.class));
                                                   // closeapp();
                                                    startActivityForResult(new Intent(android.provider.Settings.ACTION_SETTINGS), 0);
                                                }
                                            }, 1000);
                                            //
                                       }
                                        else {
                                            Toast.makeText(getApplicationContext(), "Mauvais mot de passe", Toast.LENGTH_LONG).show();
                                        }

                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();

                break;
            }

            case R.id.button15: {
                Toast.makeText(this, "Vérification de mise à jour du launcher", Toast.LENGTH_LONG).show();
                updateMyself();
                break;
            }

            case R.id.tools: {

                Intent tools=new Intent(this,SettingsToolActivity.class);
                startActivity(tools);
                break;
            }
        }
    }

    public void updateMyself ()
    {

        Intent intent = new Intent(HomeActivity.this, MainActivity.class);
        Bundle b = new Bundle();
        b.putString("packageName","com.example.info_jvs.launcher");
        b.putString("apkName","launcher.apk");
        intent.putExtras(b); //Put your id to your next Intent
        startActivity(intent);
    }

    public void closeapp() {

        this.finish();
    }

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
        startService(new Intent(this, CheckRunningActivity.class));
        initializeButton();

    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            //////// afficher une animation lors de la requète
           // pd = new ProgressDialog(HomeActivity.this);
           //pd.setMessage("");
           //pd.setCancelable(true);
           //  pd.show(); //checkserverAvaibility();

            ///////////
        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(20000);
                connection.connect();

                //str_replace(".", "", $string);
                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");

                     Log.d("Response: ", "> " + line);   //get whole response...... :-)

                }

                return buffer.toString();


            } catch (MalformedURLException e)
            {
                e.printStackTrace();
            } catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                if (connection != null)
                {
                    connection.disconnect();
                }
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //affiche l'animation
            /*if (pd.isShowing()) {
                pd.dismiss();
            }*/
            JsonString = result;
            if (result == null) {
                //pas de réseau
                //splitIt();
            } else {
                //il y a du réseau
                writeToFile(result,getApplicationContext());
                splitIt();
            }

        }
    }

}
