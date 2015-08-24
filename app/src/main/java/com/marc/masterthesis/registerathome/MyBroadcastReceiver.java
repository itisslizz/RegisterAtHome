package com.marc.masterthesis.registerathome;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
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
 * Created by marc on 10.04.15.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {
    private String state = "away";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v("OnReceive", "Received Something");
        final String action = intent.getAction();
        if (action.equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            Log.v("OnReceive", "Network state has changed");
            String new_state = "away";
            if (connectedToHome(context)) {
                new_state ="home";
            } else {
                Log.v("OnReceive", "Wifi is disconnected.");
            }
            if (new_state != state) {
                state = new_state;
                Log.i("OnReceive","State has changed");

                LogHomeData logHomeDb = new LogHomeData(context);
                logHomeDb.open();
                logHomeDb.createLogEntry(state);
                logHomeDb.close();

                Intent intentToOpen = new Intent(context, MainActivity.class);
                intentToOpen.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intentToOpen.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                String name = "state";
                intentToOpen.putExtra(name, state);
                context.startActivity(intentToOpen);
                if (new_state == "home") {
                    UpdateRemoteLogsTask task = new UpdateRemoteLogsTask(context);
                    task.execute("GO");
                }
            }
        }
    }


    private boolean connectedToHome(Context context) {
        boolean connected = false;
        String desiredMacAddress = "enter_mac_here";
        WifiManager wifiManager =
                (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        WifiInfo wifi = wifiManager.getConnectionInfo();

        if (wifi != null && wifiManager.isWifiEnabled()) {
            String macAddress = wifi.getBSSID();
            //Log.v("MBR", macAddress);
            connected = desiredMacAddress.equals(macAddress);
        }
        return connected;
    }
}
