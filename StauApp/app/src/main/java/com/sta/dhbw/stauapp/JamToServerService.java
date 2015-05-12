package com.sta.dhbw.stauapp;


import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.dhbw.jambeaconrestclient.TrafficJam;

public class JamToServerService extends IntentService
{
    private static final String TAG = JamToServerService.class.getSimpleName();


    public JamToServerService()
    {
        super("JamToServerService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        Log.i(TAG, "Sending traffic jam to server");

        Bundle extras = intent.getExtras();

        if (!extras.isEmpty())
        {
            TrafficJam jam = extras.getParcelable("jam");

        }



    }
}
