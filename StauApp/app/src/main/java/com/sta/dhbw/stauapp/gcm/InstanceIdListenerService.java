package com.sta.dhbw.stauapp.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

public class InstanceIdListenerService extends InstanceIDListenerService
{
    private static final String TAG = InstanceIdListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh()
    {
        Log.d(TAG, "Received notion to refresh GCM token. Refreshing now.");
        Intent intent = new Intent(this, RequestGcmTokenService.class);
        startService(intent);
    }

}
