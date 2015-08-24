package com.marc.masterthesis.registerathome;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.Settings;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by marc on 15.04.15.
 */
public class UpdateRemoteLogsTask extends AsyncTask<String, Void, String> {
    private Exception exception;
    private Context context;

    public UpdateRemoteLogsTask(Context context) {
        this.context = context;
    }

    protected String doInBackground(String... urls) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            String webserver_ip = "0.0.0.0"
            String address = "http://" + webserver_ip + ":1234/last_update";
            StringBuilder builder = new StringBuilder();
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(address);
            try {
                HttpResponse response = client.execute(httpGet);
                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    HttpEntity entity = response.getEntity();
                    InputStream content = entity.getContent();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                } else {
                    Log.e(MainActivity.class.toString(), "Failed JSON object");
                }
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            String answer = builder.toString();

            String readJSON = answer;
            try {
                JSONObject jsonObject = new JSONObject(readJSON);
                Log.i(MainActivity.class.getName(), jsonObject.getString("timestamp"));
                String timestamp = jsonObject.getString("timestamp");
                LogHomeData logHomedb = new LogHomeData(context);
                logHomedb.open();
                Cursor cursor = logHomedb.getLogsSince(timestamp);
                Log.i("Timestamp", timestamp);
                cursor.moveToNext();
                while (!cursor.isAfterLast()) {
                    Log.i("Reporting", "Report");
                    String state = cursor.getString(1);
                    String ts = cursor.getString(2);
                    updateRemote(state, ts);
                    cursor.moveToNext();
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.out.println("Success");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "OK";
    }

    private void updateRemote(String state, String ts) {
        String address = "http://192.168.1.115:1234/phonelog";
        HttpClient client = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(address);
        try {
            String deviceId = Settings.Secure.getString(context.getContentResolver(),
                    Settings.Secure.ANDROID_ID);
            StringEntity stringEntity = new StringEntity("{\"state\":\"" + state
                    + "\",\"timestamp\":\"" + ts
                    + "\", \"device_id\":\"" + deviceId + "\"}");
            stringEntity.setContentType("application/json");
            httpPost.setEntity(stringEntity);
            HttpResponse response = client.execute(httpPost);
            if (response.getStatusLine().getStatusCode() != 201) {
                throw new RuntimeException("Failed : HTTP error code : "
                        + response.getStatusLine().getStatusCode());
            }

            client.getConnectionManager().shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void onPostExecute(String result) {
        if (result.equals("OK")) {
            Log.i("Remote", "was Successful");
        } else {
            Log.i("Remote", "was not Successful");
        }
    }
}
