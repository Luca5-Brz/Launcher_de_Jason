package com.example.info_jvs.launcher;

import android.app.Activity;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;

import update.HTTPDataHandler;


public class MainActivity extends Activity {


    private DownloadManager downloadManager;
    private long myDownloadReference;
    String distantVersion = null;
    String currentAppVersion = null;
    int valueoflocal = 0;
    int valueofdistant = 0;
     private String urlString="";//url pour les app update

    private ProgressDialog progressdialog;
    private long enqueue;
    boolean downloading = true;
    Boolean loop = true;
    Boolean isAlerted=false;
    private Handler mHandler = new Handler();
    private Boolean data = false;
    String apkName = "";
    String packageName="";
    String apkName2="";
    String packageName2="";
    String ValueToUninstall="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Toast.makeText(this,"create mainactivity",Toast.LENGTH_LONG).show();
       // Toast.makeText(this,getApplicationInfo().dataDir,Toast.LENGTH_LONG).show();
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);


        if (mData4G != null && mData4G.isConnected()) {
            urlString="http://212.166.21.236:8080/StoreUpdateRequest2.php?NomApp=";
            //Toast.makeText(this,urlString, Toast.LENGTH_SHORT).show();

        }
        else
        {
            urlString="http://192.168.1.211/StoreUpdateRequest2.php?NomApp=";
        }

        Intent intent=getIntent();

       /// Bundle b = getIntent().getExtras();


        apkName=intent.getStringExtra("apkName");
        packageName = intent.getStringExtra("packageName");
        //Toast.makeText(this,apkName + " "+ packageName,Toast.LENGTH_LONG).show();
        if (apkName!=null && packageName != null)
        {
         checkUrlAcessiblilityBeforeExecute("http://212.166.21.236:8080/login.php");//test avec la page la plus rapide à charger
        }
    }



    public void checkUrlAcessiblilityBeforeExecute(String url) {
        final String customURL = url;
        //Toast.makeText(this,customURL,Toast.LENGTH_LONG).show();
        new Thread() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                super.run();
                try {
                    URL url = new URL(customURL);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("HEAD");
                    con.setConnectTimeout(15000);
                    con.connect();
                    Log.i("TAG", "con.getResponseCode() IS : " + con.getResponseCode());
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        //IsServerAvailable=true;
                        //Log.i("TAG", "Sucess check header "+IsServerAvailable.toString());

                        //permet de faire un job hors du thread en cours
                        runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                StartApp();
                            }
                        });

                       // IsServerAvailable=false;
                        Log.i("TAG", "Sucess check header in mainactivity");

                    }

                } catch (Exception e)
                {
                    e.printStackTrace();
                    Log.i("TAG", "fail check header");

                    runOnUiThread(new Runnable()
                    {
                        public void run()
                        {
                            Toast.makeText(MainActivity.this,"Serveur de mise à jour non trouvé",Toast.LENGTH_LONG).show();
                            final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName);
                            startActivity(launchIntent);
                        }
                    });

                }
            }

        }.start();


    }

public  void StartApp()
{
   //Toast.makeText(this,"StartApp",Toast.LENGTH_LONG).show();

    apkName2=apkName;
    packageName2=packageName;
    Log.i("TAG", apkName2);
    Log.i("TAG", packageName2);
        start();

}


    public void start() {
      //  Toast.makeText(this,"start",Toast.LENGTH_LONG).show();

        //fix bug v1.0.4 mise à jour infinie --> fichier téléchargé précédemment non effacé
        File dir = new File(Environment.getExternalStorageDirectory() + "/Android/data/com.example.info_jvs.launcher/files/Download/");
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mEthernet = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        //Toast.makeText(this,urlString+"'"+packageName2+"'",Toast.LENGTH_LONG).show();
        //Log.e("Tag",urlString+"'"+packageName2+"'");
        if (mWifi.isConnected() || mEthernet.isConnected() || mData4G.isConnected() )
        {
            //Toast.makeText(this,"start if ...",Toast.LENGTH_LONG).show();
            new ProcessJSON().execute(urlString+"'"+packageName2+"'");// récupération de la valeur distante : distantVersion

            Boolean isinstalledApp = update.Utils.checkForInstalledApp(this, packageName2);

            //Récupérer la version
            currentAppVersion = update.Utils.getAppVersionName(this, packageName2);
            Log.i("TAG", "current installed app:"+ apkName2);
            //si elle est installée, on converti le string en int pour la comparer avec la version en réseaux
            if (isinstalledApp == true) {
                String s = currentAppVersion;
                String[] parts = s.split("\\."); // escape .
                String part1 = parts[0];
                String part2 = parts[1];
                String part3 = "";
                if (parts.length > 2 && parts[2] != null) {
                    part3 = parts[2];
                } else {
                    part3 = "0";
                }
                loop = true;
               // Toast.makeText(this,"start if installed true",Toast.LENGTH_LONG).show();
                String last = part1 + part2 + part3;
                // Toast.makeText(this,last,Toast.LENGTH_LONG).show();
                Log.i("TAG", "valeur locales: "+currentAppVersion +" / "+ part1 + part2 + part3 +";");
                valueoflocal = Integer.valueOf(String.valueOf(last));
                Log.i("TAG", "valeur locale: "+valueoflocal);
            }


            //récupération de la version du fichier apk en ligne
            int i = 0;
            ///tant que le serveur ne répond pas on boucle

            // le ProcessJSON peut parfois prendre du temps (utilise un thread différent)
            String looped="";
            while (loop == true)
            {
                i = i + 1;
                looped="start loop "+i;
                //Log.e("lol",looped);

                if (distantVersion != null) {
                    //Toast.makeText(this,"start loop if != ",Toast.LENGTH_LONG).show();
                    //Toast.makeText(this,distantVersion,Toast.LENGTH_LONG).show();
                    String currentString = distantVersion;
                    Log.i("TAG", "current string :"+currentString);
                    String[] separated = currentString.split(";");
                    String s2 = separated[0];

                    ValueToUninstall = separated[1];
                    String[] parts_2 = s2.split("\\."); // escape .
                    String part1_2 = parts_2[0];
                    String part2_2 = parts_2[1];
                    String part3_2 = parts_2[2];
                    String last = part1_2 + part2_2 + part3_2;
                    Log.i("TAG", "valeur de last online: "+last);
                    valueofdistant = Integer.valueOf(String.valueOf(last));
                    loop = false;
                    //  Toast.makeText(this,String.valueOf(isinstalledApp),Toast.LENGTH_SHORT).show();


                    String[] unistall_2 = ValueToUninstall.split("\\."); // escape .
                    String unistall1_2 = unistall_2[0];
                    String unistall2_2 = unistall_2[1];
                    String unistall3_2 = unistall_2[2];
                    ValueToUninstall = unistall1_2 + unistall2_2 + unistall3_2;
                    Log.i("TAG", "valeur à désinstaller online: "+ValueToUninstall);
                    startProcess();
                }

            }

        }
        else
        {

            Toast.makeText(this, "Réseau non disponnible", Toast.LENGTH_SHORT).show();
            closeapp();

        }
    }


    //initialisation du téléchargement (+vérif connection réseau)
    public void startProcess() {
       // Toast.makeText(this,"startProcess",Toast.LENGTH_LONG).show();

        //vérification de l'accès au réseaux avant de télécharger
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mEthernet = connManager.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);

       // if (mWifi.isConnected() || mEthernet.isConnected()) {
//si la version distante est inférieur --> on désinstalle / réinstalle
            if (valueofdistant < valueoflocal)
            {
                Log.e("Mainactivity","inférieur");
                Intent intent = new Intent(Intent.ACTION_DELETE);
                intent.setData(Uri.parse("package:"+packageName2));
                startActivity(intent);
                Boolean isinstalledApp = update.Utils.checkForInstalledApp(this,packageName2);

                while (isinstalledApp != true)
                {

                }

                download();
            }
            //si supérieur, on télécharge la mise à jour
            if (  valueofdistant > valueoflocal)
            {
                Log.e("Mainactivity","supérieur");
                Log.e("valeurs: ",valueoflocal+" : "+valueofdistant);
                final Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName2);

                if (isAlerted.equals(false))
                {
                    AlertDialog.Builder builder;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
                    }
                    else
                    {
                        builder = new AlertDialog.Builder(this);
                    }
                   // Toast.makeText(this,valueoflocal+" < "+valueofdistant,Toast.LENGTH_LONG).show();

                    //unistall if 2.1.4
                    Log.e("TAG", "packagename: "+packageName2);
                    if (packageName2.equals("com.computerland.cdh.mobile"))
                    {
                        if (valueoflocal == Integer.valueOf(ValueToUninstall))
                        {
                            Log.e("TAG", "à désinstaller: "+ValueToUninstall);
                            Intent intent = new Intent(Intent.ACTION_DELETE);
                            intent.setData(Uri.parse("package:" + packageName2));
                            startActivity(intent);
                        }
                    }
                    builder.setTitle("Mise à jour disponnible !")
                            .setMessage("Voulez-vous la télécharger?\nPour la mise à jour, raprochez-vous d'un point d'accès wifi ")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue
                                 isAlerted=true;
                                    sendLog();
                                    download();


                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    startActivity(launchIntent);
                                }
                            })
                            //.setIcon(android.R.drawable.stat_sys_warning)
                            .show();

                }
                else
                {
                    Log.e("Mainactivity","égal");
                    sendLog();
                    download();
                }
            }
       else
        {
                sendLog();
                Toast.makeText(this, "Application à jour", Toast.LENGTH_LONG).show();
                //  Toast.makeText(this,String.valueOf(valueofdistant),Toast.LENGTH_LONG).show();
                closeapp();
        }

       // }
       // else {
       //     startProcess();
            // Toast.makeText(this,"Pas d'accès au réseaux",Toast.LENGTH_LONG).show();
            //  closeapp();
        //}

    }


    public void Check() {
        // Récupère la version de l'app en ligne


       //Toast.makeText(this,"check",Toast.LENGTH_LONG).show();

        loop = true;
        Toast.makeText(this, " Vérification de l'installation ... ", Toast.LENGTH_SHORT).show();

        new ProcessJSON().execute(urlString+"'"+packageName2+"'");

        Boolean isinstalledApp = update.Utils.checkForInstalledApp(this, packageName2);

        //Récupérer la version de cdhMobile
        currentAppVersion = update.Utils.getAppVersionName(this, packageName2);

        if (isinstalledApp == true) {
            String s = currentAppVersion;
            String[] parts = s.split("\\."); // escape .
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = "";
            if (parts.length > 2 && parts[2] != null) {
                part3 = parts[2];
            } else {
                part3 = "0";
            }
            String last = part1 + part2 + part3;
            valueoflocal = Integer.valueOf(String.valueOf(last));

        }

        int i = 0;
///tant que le serveur ne répond pas on boucle
        while (loop == true) {
            i = i + 1;
            if (distantVersion != "") {

                String s2 = distantVersion;
                Log.e("TAG","distantversion "+distantVersion);
                String[] parts_2 = s2.split("\\."); // escape .

                String part1_2 = parts_2[0];

                String part2_2 = parts_2[1];
                String part3_2 = parts_2[2];
                String last = part1_2 + part2_2 + part3_2;
                Log.e("TAG","distantversion "+last);

                valueofdistant = Integer.valueOf(String.valueOf(last));
                Log.e("TAG","distantversion "+last);
                loop = false;

               // start();
            }

        }

    }


    public void sendLog() {
        //Toast.makeText(this,"Sendlog",Toast.LENGTH_LONG).show();
        String versionCodeOfMyApp = update.Utils.getAppVersionName(this, "com.example.info_jvs.launcher");
        String deviceId = Secure.getString(this.getContentResolver(), Secure.ANDROID_ID);
        //Toast.makeText(this, deviceId, Toast.LENGTH_SHORT).show();
        String IP = update.Utils.getIPAddress(true);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        //Toast.makeText(this,"senlog",Toast.LENGTH_LONG).show();
        String IMEI="";
        if(getResources().getBoolean(R.bool.isTab))
        {}
        else
        {
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = telephonyManager.getDeviceId();
            //IMEI=telephonyManager.toString();
        }
        if (IMEI.isEmpty())
        {
            IMEI=Build.SERIAL;
        }

                new logToServer().execute("http://212.166.21.236:8080/updateCDH.php?IP=" + IP + "&versionCDH=" + currentAppVersion + "&deviceID=" + deviceId + "&VersionLauncher=" + versionCodeOfMyApp+ "&IMEI=" + IMEI );
               // Toast.makeText(this,"http://212.166.21.236:8080/updateCDH.php?IP=" + IP + "&versionCDH=" + currentAppVersion + "&deviceID=" + deviceId,Toast.LENGTH_LONG).show();

        Log.d("log launcher version","http://212.166.21.236:8080/updateCDH.php?IP=" + IP + "&versionCDH=" + currentAppVersion + "&deviceID=" + deviceId + "&VersionLauncher=" + versionCodeOfMyApp+"&IMEI=" + IMEI );
    }

    public void download() {
        //Toast.makeText(this,"Download",Toast.LENGTH_LONG).show();

        File f = new File("/storage/emulated/Android/data/com.example.info_jvs.launcher/files/Download/"+apkName2);
        if (f.exists()) {
            f.delete();
        }

        Intent intent2 = new Intent(Intent.ACTION_VIEW);
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        NetworkInfo mData4G = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        Uri uri=uri = Uri.parse("http://212.166.21.236:8080/Store/"+apkName2);


        final DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS,apkName2);
        myDownloadReference = downloadManager.enqueue(request);

        //////////////////
        ///progressBar///
        ////////////////
        final ProgressDialog progressBarDialog = new ProgressDialog(this);
        progressBarDialog.setTitle("Téléchargement de la mise à jour ...");
        progressBarDialog.setMessage("À la fin du téléchargement, appuyez sur installer");

        progressBarDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog.setCancelable(false);

        progressBarDialog.setProgress(0);

        new Thread(new Runnable() {

            @Override
            public void run() {

                boolean downloading = true;

                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                while (downloading) {

                    DownloadManager.Query q = new DownloadManager.Query();
                    q.setFilterById(myDownloadReference); //filter by id which you have receieved when reqesting download from download manager
                    Cursor cursor = manager.query(q);
                    cursor.moveToFirst();
                    int bytes_downloaded = cursor.getInt(cursor
                            .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                    int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

                    if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                        downloading = false;
                    }

                    final int dl_progress = (int) ((bytes_downloaded * 100l) / bytes_total);

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

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
        // surveille la fin du téléchargement avant de l'éxécuter///
        ///////////////////////////////////////////////////////////
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, Intent intent) {

                final long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (myDownloadReference == reference) {
                    //attendre 1 seconde avant d'éxécuter
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            //executer le fichier
                            String  dir = Environment.getExternalStorageDirectory() + "/Android/data/com.example.info_jvs.launcher/files/Download/";

                            File file = new File(dir, apkName2);
                            Intent promt = new Intent(Intent.ACTION_VIEW);
                            promt.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                            promt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            promt.putExtra(Intent.EXTRA_RETURN_RESULT, true);
                            startActivity(promt);
                            downloading = false;
                            progressBarDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Check();

                                }
                            });
                            progressBar();

                            // closeapp();
                        }
                    }, 600);
                }

            }
        };
        registerReceiver(receiver, filter);
    }


    public void closeapp() {
        if (data == true) {
            WifiManager wManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            wManager.setWifiEnabled(false);
        }
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(packageName2);//arraylist pour le nom de l'app?
        startActivity(launchIntent);
        this.finish();
    }

    public void progressBar() {

        final ProgressDialog progressBarDialog2 = new ProgressDialog(this);
        progressBarDialog2.setTitle("Téléchargement terminé");
        progressBarDialog2.setMessage("Appuyez sur Fermer");

        // progressBarDialog2.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressBarDialog2.setCancelable(false);

        //progressBarDialog2.setMax(100);
        //  progressBarDialog2.setProgress(100);

        progressBarDialog2.setButton(DialogInterface.BUTTON_NEGATIVE, "Fermer", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Check();
               // HomeActivity home=new HomeActivity();
              //  home.initializeButton();
            }
        });
        progressBarDialog2.show();

    }

    private class ProcessJSON extends AsyncTask<String, Void, String> {
        protected String doInBackground(String... strings) {
            String stream = null;
            String urlString = strings[0];

            update.HTTPDataHandler hh = new HTTPDataHandler();
            stream = hh.GetHTTPData(urlString);
            distantVersion = stream;
            // Return the data from specified url


            return stream.toString();
        }

    }

}

