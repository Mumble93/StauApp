package com.sta.dhbw.stauapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.sta.dhbw.jambeaconrestclient.ICallBackInterface;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;
import com.sta.dhbw.stauapp.dialogs.ConnectionIssueDialogFragment;
import com.sta.dhbw.stauapp.util.Utils;
import com.sta.dhbw.stauapp.util.Utils.ConnectionIssue;

import java.util.List;

import static com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient.AvailabilityTask;

public class SplashScreen extends FragmentActivity implements ICallBackInterface
{
    private static final String TAG = SplashScreen.class.getSimpleName();

    private Intent intent;

    ProgressDialog progressDialog;

    @Override
    public void onRegisterComplete(String xRequestId)
    {

    }

    @Override
    public void onUserUpdateComplete(String updatedXRequestId)
    {

    }

    @Override
    public void onGetTrafficJamComplete(TrafficJam trafficJam)
    {

    }

    @Override
    public void onGetJamListComplete(List<TrafficJam> trafficJamList)
    {

    }

    @Override
    public void onTrafficJamUpdateComplete(TrafficJam updatedJam)
    {

    }

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
            fragment.show(getSupportFragmentManager(), "dialog");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        JamBeaconRestClient restClient = new JamBeaconRestClient();

        intent = new Intent(this, MainActivity.class);
        intent.putExtra("client", restClient);

        //Check GPS availability
        if (Utils.checkGps(this))
        {
            //Check internet connection
            if (!Utils.checkInternetConnection(this))
            {
                DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.NETWORK_NOT_AVAILABLE);
                fragment.show(getSupportFragmentManager(), "dialog");
            } else
            {
                AvailabilityTask availabilityTask = new AvailabilityTask(this);
                availabilityTask.execute();
            }
        } else
        {
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.GPS_NOT_AVAILABLE);
            fragment.show(getSupportFragmentManager(), "dialog");
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
