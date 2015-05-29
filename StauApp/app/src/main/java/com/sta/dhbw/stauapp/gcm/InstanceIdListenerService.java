package com.sta.dhbw.stauapp.gcm;

import android.content.Intent;

import com.google.android.gms.iid.InstanceIDListenerService;

public class InstanceIdListenerService extends InstanceIDListenerService
{
    private static final String TAG = InstanceIdListenerService.class.getSimpleName();

    @Override
    public void onTokenRefresh()
    {
        Intent intent = new Intent(this, RequestGcmTokenService.class);
        startService(intent);
    }

}
