package com.sta.dhbw.stauapp;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.sta.dhbw.stauapp.exception.JamBeaconException;
import com.sta.dhbw.stauapp.jam.TrafficJam;

import java.io.IOException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class JamToServerService extends IntentService
{
    private static final String TAG = JamToServerService.class.getSimpleName();

    private static final String SERVER_ADDRESS = "someServerAddress";

    public JamToServerService()
    {
        super("JamToServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Bundle extras = intent.getExtras();

        if (!extras.isEmpty())
        {
            TrafficJam jam = extras.getParcelable("jam");

        }


    }

    private HttpsURLConnection openConnection() throws JamBeaconException
    {
        HttpsURLConnection connection;

        try
        {
            URL url = new URL(SERVER_ADDRESS);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e)
        {
            Log.e(TAG, "Error trying to open connection to server");
            throw new JamBeaconException(e.getMessage(), e);
        }

        return connection;
    }
}
