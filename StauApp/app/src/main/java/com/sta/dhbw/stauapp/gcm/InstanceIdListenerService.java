package com.sta.dhbw.stauapp.gcm;

import android.content.Intent;
import android.util.Log;

import com.google.android.gms.iid.InstanceIDListenerService;

/**
 * Listens for the com.google.android.gms.iid.InstanceID Intent that is sent when a token needs
 * to be refreshed.
 */
public class InstanceIdListenerService extends InstanceIDListenerService
{
    private static final String TAG = InstanceIdListenerService.class.getSimpleName();

    /**
     * Starts the RequestGcmTokenService to retrieve a new Token.
     */
    @Override
    public void onTokenRefresh()
    {
        Log.d(TAG, "Received notion to refresh GCM token. Refreshing now.");
        Intent intent = new Intent(this, RequestGcmTokenService.class);
        startService(intent);
    }

}
