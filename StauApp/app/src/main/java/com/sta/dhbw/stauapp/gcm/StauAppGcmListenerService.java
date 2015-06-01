package com.sta.dhbw.stauapp.gcm;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.gcm.GcmListenerService;

public class StauAppGcmListenerService extends GcmListenerService
{
    private static final String TAG = StauAppGcmListenerService.class.getSimpleName();

    @Override
    public void onMessageReceived(String from, Bundle data)
    {
        String message = data.getString("message");
        Log.d(TAG, "Received message from " + from + ". Message was " + message);
    }
}
