package com.example.info_jvs.launcher;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

/**
 * Created by info-jvs on 04-04-17.
 */

class logToServer extends AsyncTask<String,String,String> {
    protected void onPostExecute(String result) {
        // TODO Auto-generated method stub
        super.onPostExecute(result);
        // update textview here
        System.out.println("onPostExecute");
        System.out.println("onPostExecute: result " + result);
    }

    @Override
    protected void onPreExecute() {
        // TODO Auto-generated method stub
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... params) {

        System.out.println("doInBackground");

        try
        {
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost method = new HttpPost(params[0]);
            HttpResponse response = httpclient.execute(method);
            HttpEntity entity = response.getEntity();
            if(entity != null){
                return EntityUtils.toString(entity);
            }
            else{
                return "No string.";
            }
        }
        catch(Exception e){
            return "Network problem";
        }
    }
}
