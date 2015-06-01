package com.sta.dhbw.stauapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;

import com.sta.dhbw.jambeaconrestclient.IHeartbeatCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.stauapp.dialogs.ConnectionIssueDialogFragment;
import com.sta.dhbw.stauapp.util.Utils;
import com.sta.dhbw.stauapp.util.Utils.ConnectionIssue;

public class SplashScreen extends Activity implements IHeartbeatCallback
{
    private static final String TAG = SplashScreen.class.getSimpleName();

    private JamBeaconRestClient restClient = new JamBeaconRestClient();

    private Intent intent;

    ProgressDialog progressDialog;

    @Override
    public void onCheckComplete(boolean success)
    {
        if (progressDialog.isShowing())
        {
            progressDialog.dismiss();
        }

        if (success)
        {
            startActivity(intent);
            finish();
        } else
        {
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.SERVER_NOT_AVAILABLE);
            fragment.show(getFragmentManager(), "dialog");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        intent = new Intent(this, MainActivity.class);

        //Check GPS availability
        if (Utils.checkGps(this))
        {
            //Check internet connection
            if (!Utils.checkInternetConnection(this))
            {
                progressDialog.dismiss();
                DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.NETWORK_NOT_AVAILABLE);
                fragment.show(getFragmentManager(), "dialog");
            } else
            {
                restClient.checkServerAvailability(this);
            }
        } else
        {
            progressDialog.dismiss();
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.GPS_NOT_AVAILABLE);
            fragment.show(getFragmentManager(), "dialog");
        }

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setIndeterminate(true);
        if (android.os.Debug.isDebuggerConnected())
        {
            progressDialog.setMessage("Welcome, developer...");
        } else
        {
            progressDialog.setMessage("Some work is done here...");
        }
        progressDialog.show();
    }
}
