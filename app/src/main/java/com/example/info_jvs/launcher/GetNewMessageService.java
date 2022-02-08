package com.example.info_jvs.launcher;

import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.WindowManager;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by info-jvs on 20-02-18.
 */

public class GetNewMessageService extends Service {


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("tagii","start message service");
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate
                (new Runnable() {
                    public void run() {
                        String deviceId = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
                        try
                        {
                            sendLog(deviceId);
                            //URLEncoder.encode(uri, "UTF-8");
                        } catch (UnsupportedEncodingException e) {
                            Log.e("Launcher", "UnsupportedEncodingException");
                        }
                    }
                }, 0, 5, TimeUnit.MINUTES);
    }
String NewMessage=null;
    public  void  sendLog(String deviceID)  throws  UnsupportedEncodingException
    {
        Log.e("tagii","url");

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String>
        {

            @Override
            protected String doInBackground(String... params)
            {
                String paramdeviceID = params[0];

                //System.out.println("*** doInBackground ** paramdeviceID "+ paramdeviceID);

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost("http://212.166.21.236:8080/ReadMessages.php?DeviceID="+ paramdeviceID);

                List<NameValuePair> nameValuePairList = new ArrayList<>(3);
                nameValuePairList.add(new BasicNameValuePair("deviceID", paramdeviceID));
                try {
                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairList));
                    HttpResponse response = httpClient.execute(httpPost);

                    // Print out the response message
                   // System.out.println(EntityUtils.toString(response.getEntity()));

                    String MessageResponse= EntityUtils.toString(response.getEntity());
                    Integer sizeOfMessage=MessageResponse.length();
                    if (sizeOfMessage.equals(8))
                    {
                        NewMessage=null;
                        System.out.println("Pas de messages !!!");
                    }
                    else
                    {

                        //HomeActivity home=new HomeActivity();
                        //khome.AlertDialog(MessageResponse);
                        System.out.println(sizeOfMessage);
                        NewMessage=MessageResponse;
                        System.out.println(MessageResponse);
                    }
                    //ici faire un alertbox
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (NewMessage!=null)
                {
                    String str = NewMessage.replaceFirst("^ *", "");
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getApplicationContext())
                            .setTitle("Attention !!")
                            .setMessage(str)
                            .setPositiveButton("Message compris !",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog,
                                                            int which) {
                                            // Toast.makeText(getApplicationContext(),"Yes is clicked",Toast.LENGTH_LONG).show();
                                        }
                                    })
                            .create();

                    alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                    alertDialog.show();
                    NewMessage=null;
                }
                else
                {

                }

                super.onPostExecute(result);
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();
        sendPostReqAsyncTask.execute(deviceID);
    }
}
