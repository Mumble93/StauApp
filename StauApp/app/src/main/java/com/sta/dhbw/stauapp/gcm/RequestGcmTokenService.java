package com.sta.dhbw.stauapp.gcm;

import android.app.AlertDialog;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.sta.dhbw.jambeaconrestclient.IUserCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.stauapp.R;
import com.sta.dhbw.stauapp.dialogs.RestIssueBroadcastReceiver;
import com.sta.dhbw.stauapp.settings.PrefFields;

import java.io.IOException;

public class RequestGcmTokenService extends IntentService implements IUserCallback
{
    public static final String TAG = RequestGcmTokenService.class.getSimpleName();
    private SharedPreferences sharedPreferences;
    private ProgressDialog progressDialog;


    public RequestGcmTokenService()
    {
        super("RequestGcmTokenService");
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Registrierung am Server wird abgeschlossen...");
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

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
            try
            {
                Log.d(TAG, "Updating registered user.");
                restClient.updateUser(oldToken, token, existingRequestId, this);
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
                Log.d(TAG, "Registering new User");
                restClient.registerUser(token, this);
            } catch (JamBeaconException e)
            {
                Intent intent = new Intent().setAction(RestIssueBroadcastReceiver.REST_ISSUE)
                        .putExtra("reason", e.getMessage());
                this.sendBroadcast(intent);
            }
        }
    }

    @Override
    public void onRegisterComplete(String xRequestId)
    {
        if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }

        if (null == xRequestId || xRequestId.isEmpty())
        {
            getAlertDialog(getString(R.string.registration_failed), getString(R.string.registration_failed_message)).show();

        } else
        {

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(PrefFields.PROPERTY_X_REQUEST_ID, xRequestId)
                    .putBoolean(PrefFields.SENT_TOKEN_TO_SERVER, true).apply();
        }

    }

    @Override
    public void onUserUpdateComplete(String updatedXRequestId)
    {
        if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }

        if (null == updatedXRequestId || updatedXRequestId.isEmpty())
        {
            getAlertDialog(getString(R.string.update_failed), getString(R.string.registration_update_failed))
                    .show();
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(PrefFields.PROPERTY_X_REQUEST_ID, updatedXRequestId).
                putBoolean(PrefFields.SENT_TOKEN_TO_SERVER, true).apply();
    }

    private AlertDialog getAlertDialog(String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNegativeButton(getString(R.string.dialog_cancel), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                dialog.dismiss();
                RequestGcmTokenService.this.startActivity(intent);
            }
        });
        builder.setPositiveButton(getString(R.string.dialog_retry), new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Intent intent = new Intent(RequestGcmTokenService.this, RequestGcmTokenService.class);
                RequestGcmTokenService.this.startService(intent);
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }

}
