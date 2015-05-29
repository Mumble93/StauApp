package com.sta.dhbw.stauapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.stauapp.R;
import com.sta.dhbw.stauapp.dialogs.RestIssueBroadcastReceiver;
import com.sta.dhbw.stauapp.settings.PrefFields;

import java.io.IOException;

public class RequestGcmTokenService extends IntentService
{
    public static final String TAG = RequestGcmTokenService.class.getSimpleName();
    final SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());


    public RequestGcmTokenService()
    {
        super("RequestGcmTokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        //Check if a token has already been sent to the server
        //This determines whether a POST or a PUT request is issued
        boolean alreadySentToServer = sharedPreferences.getBoolean(PrefFields.SENT_TOKEN_TO_SERVER, false);

        try
        {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token: " + token);

            String xRequestId = sendTokenToServer(token, alreadySentToServer);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString(PrefFields.PROPERTY_REG_ID, token)
                    .putString(PrefFields.PROPERTY_X_REQUEST_ID, xRequestId);
            if (!alreadySentToServer)
            {
                editor.putBoolean(PrefFields.SENT_TOKEN_TO_SERVER, true).apply();
            }
            editor.apply();
        } catch (IOException e)
        {
            String error = "ERROR getting Registration Token: " + e.getMessage();
            Log.e(TAG, error);

        }
    }

    private String sendTokenToServer(String token, boolean isUpdate)
    {
        JamBeaconRestClient restClient = new JamBeaconRestClient();
        String xRequestId = "";
        if (isUpdate)
        {
            String oldToken = sharedPreferences.getString(PrefFields.PROPERTY_REG_ID, "");
            String existingRequestId = sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, "");
            try
            {
                xRequestId = restClient.updateUser(oldToken, token, existingRequestId);
            } catch (JamBeaconException e)
            {
                Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_ISSUE)
                        .putExtra("reason", e.getMessage());
                this.sendBroadcast(intent);
            }
        } else
        {
            try
            {
                xRequestId = restClient.registerUser(token);
            } catch (JamBeaconException e)
            {
                Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_ISSUE)
                        .putExtra("reason", e.getMessage());
                this.sendBroadcast(intent);
            }
        }
        return xRequestId;
    }
}
