package com.example.info_jvs.launcher;

import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.Toast;

import java.io.File;

public class SettingsToolActivity extends AppCompatActivity {

    int brightnessmode;
    int rotationmode;

    SeekBar notification ;
    Toolbar toolbar;
    AudioManager audioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_tool);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        notification = findViewById(R.id.notification);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        final int musicVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

        notification.setMax(7);
        notification.setProgress(musicVolume);
        //Toast.makeText(this,String.valueOf(musicVolume),Toast.LENGTH_LONG).show();
       // notification.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION));

        notification.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, i*2, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, i, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_ALARM, i, 0);
                audioManager.setStreamVolume(AudioManager.STREAM_RING,i,0);
                audioManager.setStreamVolume(AudioManager.STREAM_SYSTEM,i,0);

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        setSupportActionBar(toolbar);
        //Toast.makeText(this,"oncreate",Toast.LENGTH_LONG).show();

        getBrightMode();
        getRotationMode();
        Switch automaticLuminosity = (Switch) findViewById(R.id.automaticLuminosity);
        Switch automaticRotation = (Switch) findViewById(R.id.automaticRotation);



        if (brightnessmode == 1) {
            //mettre le bouton sur on
            automaticLuminosity.setChecked(true);
           // Toast.makeText(this, "brightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();
        }

        if (brightnessmode == 0) {
            //below code will on the auto mode
            automaticLuminosity.setChecked(false);
            //Toast.makeText(this, "brightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();
        }

        if (rotationmode == 1) {
            // on
            automaticRotation.setChecked(true);
        }
        if (rotationmode == 0) {
            //off
            automaticRotation.setChecked(false);
        }

        StartButtonListener();
    }



    public void StartButtonListener () {

        Switch automaticLuminosity = findViewById(R.id.automaticLuminosity);
        automaticLuminosity.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked)
            {
                // quand le bouton est checké, on modifie le BrightMode
                setBrightMode();
            }
        });

        Switch automaticRotation = (Switch) findViewById(R.id.automaticRotation);
        automaticRotation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,boolean isChecked) {

                setRotationtMode();

            }
        });

        Button installAddon=(Button) findViewById(R.id.installAddon);
        installAddon.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {


               download("AddOn.apk");

            }
        });


        Button repair4G=(Button) findViewById(R.id.Retablir4G);
        repair4G.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                check4GNetwork();
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
        });
        Button repairWIFI=(Button) findViewById(R.id.RetablirWifi);
        repairWIFI.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                checkWifiNetwork();
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
        });


        Button selectKeyboard=(Button) findViewById(R.id.selectKeyboard);
        selectKeyboard.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                InputMethodManager ime=(InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                if(ime!=null) {
                    ime.showInputMethodPicker();
                }

            }
        });

    }
    public void checkNetwork() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

//vérifie si c'est pas une tablette ou pas (plantage car pas de 4g sur tablette)
        if (getResources().getBoolean(R.bool.isTab)) {
            // Toast.makeText(this,"Tablette",Toast.LENGTH_LONG).show();

            if (mData4G != null && mData4G.isConnected()) {

                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wManager.setWifiEnabled(false);
                // Toast.makeText(this,"ICI 4",Toast.LENGTH_LONG).show();


                //  Toast.makeText(this,"4g",Toast.LENGTH_LONG).show();
            } else if (mData4G == null && mWifi == null) {
                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wManager.setWifiEnabled(true);
                // Toast.makeText(this,"only wifi",Toast.LENGTH_LONG).show();
            } else {
                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            }

        } else {
            // Toast.makeText(this,"mobile",Toast.LENGTH_LONG).show();
            if (mData4G != null && mData4G.isConnected()) {

                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wManager.setWifiEnabled(false);
                // Toast.makeText(this,"ICI 1",Toast.LENGTH_LONG).show();


            } else if (mData4G == null && mWifi == null) {
                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wManager.setWifiEnabled(true);

            } else if (mData4G.isConnectedOrConnecting()) {
                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                wManager.setWifiEnabled(false);
                // Toast.makeText(this,"ICI 3",Toast.LENGTH_LONG).show();

            } else {
                WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            }

            if (mData4G != null && mData4G.isConnected()) {

                //Toast.makeText(this,"ICI5",Toast.LENGTH_LONG).show();

            } else {

            }


        }
        WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wManager.setWifiEnabled(false);
    }

    public void check4GNetwork() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (mData4G != null) {

            wManager.setWifiEnabled(false);
            // Toast.makeText(this,"ICI 1",Toast.LENGTH_LONG).show();
            Toast.makeText(this,"wifi coupé",Toast.LENGTH_LONG).show();

        }
        else
        {
            Toast.makeText(this,"Pas de 4G disponnible",Toast.LENGTH_LONG).show();

        }

    }


    public void checkWifiNetwork() {

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wManager.setWifiEnabled(true);
        Toast.makeText(this,"wifi allumé",Toast.LENGTH_LONG).show();


    }

    //This method will give brightness mode
        //if brigthnessmode=0 means Auto mode is currently off
        //if brightnessmode=1 means Auto mode is currently on
    protected void getBrightMode() {
        try {
            brightnessmode = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception e) {
            Log.d("tag", e.toString());
        }
    }

    protected void setBrightMode() {

        getBrightMode();
        if (brightnessmode == 1) {
            // on
            //Toast.makeText(this,"Activé",Toast.LENGTH_LONG).show();
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            // Toast.makeText(this, "set brightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();

        }
        if (brightnessmode == 0) {
            //off
            //Toast.makeText(this,"Désactivé",Toast.LENGTH_LONG).show();
            android.provider.Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            // Toast.makeText(this, "setbrightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();

        }
    }

    protected void getRotationMode() {

        if (Settings.System.getInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
        {
          //  Toast.makeText(getApplicationContext(), "Auto Rotate is ON", Toast.LENGTH_SHORT).show();
            rotationmode=1;
        }
        else
        {
           // Toast.makeText(getApplicationContext(), "Auto Rotate is OFF", Toast.LENGTH_SHORT).show();
            rotationmode=0;
        }
    }

    protected void setRotationtMode() {
// TODO Auto-generated method stub
        getRotationMode();
            if (rotationmode == 0) {
                // on
                //Toast.makeText(this,"set Activé",Toast.LENGTH_LONG).show();
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1);

              //  Toast.makeText(this, "set brightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();

            }
            if (rotationmode == 1) {
                //off
               // Toast.makeText(this,"set Désactivé",Toast.LENGTH_LONG).show();
                Settings.System.putInt(getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

               // Toast.makeText(this, "setbrightnessmode "+brightnessmode ,Toast.LENGTH_LONG).show();

            }


    }






    private DownloadManager downloadManager;
    private long myDownloadReference;
    String storeUrl="http://212.166.21.236:8080/Store/";
    boolean downloading = true;
    //téléchargement des app non installées (n'est pas utilisé pour les update !! )
    String path="";
    public void download(final String filename) {

         path="";
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
            // Do something for JellyBean 4.2.2
            path = "/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download";

        }

        else if (currentapiVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
            // Do something for JellyBean 5.1
            path = "/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download";


        }
        else{
            // do something for phones running an SDK above JellyBean
            path = "/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download";
        }

        Toast.makeText(this,path,Toast.LENGTH_LONG).show();
        Log.d("path",path);
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
                            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
                            if (currentapiVersion <= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1){
                                // Do something for JellyBean and above versions
                                dir = path;

                            }
                            else if (currentapiVersion <= Build.VERSION_CODES.LOLLIPOP_MR1){
                                // Do something for lollipop and above versions
                                dir = Environment.getExternalStorageDirectory() + "/Android/data/com.example.info_jvs.launcher/files/Download/";

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
                                    //initializeButton();
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

    public void progressBar()
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
                //initializeButton();
            }
        });
        progressBarDialog2.show();

    }
}
