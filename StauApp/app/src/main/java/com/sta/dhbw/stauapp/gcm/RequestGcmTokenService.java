package com.sta.dhbw.stauapp.gcm;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.sta.dhbw.jambeaconrestclient.IUserCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.stauapp.MainActivity.RestIssueBroadcastReceiver;
import com.sta.dhbw.stauapp.R;
import com.sta.dhbw.stauapp.settings.PrefFields;

import java.io.IOException;

public class RequestGcmTokenService extends IntentService implements IUserCallback
{
    public static final String TAG = RequestGcmTokenService.class.getSimpleName();
    private SharedPreferences sharedPreferences;


    public RequestGcmTokenService()
    {
        super("RequestGcmTokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Check if a token has already been sent to the server
        //This determines whether a POST or a PUT request is issued
        boolean alreadySentToServer = sharedPreferences.getBoolean(PrefFields.SENT_TOKEN_TO_SERVER, false);

        try
        {
            InstanceID instanceID = InstanceID.getInstance(this);
            String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                    GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
            Log.d(TAG, "GCM Registration Token: " + token);

            SharedPreferences.Editor editor = sharedPreferences.edit();

            sendTokenToServer(token, alreadySentToServer);

            editor.putString(PrefFields.PROPERTY_REG_ID, token).apply();
        } catch (IOException e)
        {
            String error = "ERROR getting Registration Token: " + e.getMessage();
            Log.e(TAG, error);

        }
    }

    private void sendTokenToServer(String token, boolean isUpdate)
    {
        JamBeaconRestClient restClient = new JamBeaconRestClient();
        if (isUpdate)
        {
            String oldToken = sharedPreferences.getString(PrefFields.PROPERTY_REG_ID, "");
            String existingRequestId = sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, "");
            Log.d(TAG, "Updating registered user.");
            restClient.updateUser(oldToken, token, existingRequestId, this);
        } else
        {
            Log.d(TAG, "Registering new User");
            restClient.registerUser(token, this);
        }
    }

    @Override
    public void onRegisterComplete(String xRequestId)
    {
        if (null == xRequestId || xRequestId.isEmpty())
        {
            Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_EVENT)
                    .putExtra("success", false)
                    .putExtra("reason", getString(R.string.registration_failed));
            this.sendBroadcast(intent);
        } else
        {
            Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_EVENT)
                    .putExtra("success", true)
                    .putExtra("reason", getString(R.string.registration_success_message));
            this.sendBroadcast(intent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PrefFields.PROPERTY_X_REQUEST_ID, xRequestId)
                    .putBoolean(PrefFields.SENT_TOKEN_TO_SERVER, true).apply();
        }

    }

    @Override
    public void onUserUpdateComplete(String updatedXRequestId)
    {
        if (null == updatedXRequestId || updatedXRequestId.isEmpty())
        {
            Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_EVENT)
                    .putExtra("success", false)
                    .putExtra("reason", getString(R.string.registration_failed));
            this.sendBroadcast(intent);
        } else
        {
            Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_EVENT)
                    .putExtra("success", true)
                    .putExtra("reason", getString(R.string.registration_success_message));
            this.sendBroadcast(intent);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PrefFields.PROPERTY_X_REQUEST_ID, updatedXRequestId).
                    putBoolean(PrefFields.SENT_TOKEN_TO_SERVER, true).apply();
        }
    }

    @Override
    public void onUserUnregister(Integer resultCode)
    {

    }
}
