package com.sta.dhbw.stauapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.sta.dhbw.stauapp.Utils.ConnectionIssue;

public class SplashScreen extends FragmentActivity
{
    Context context;
    private static final String TAG = SplashScreen.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = getApplicationContext();

        final Intent intent = new Intent(context, MainActivity.class);

        //Check GPS availability
        if (Utils.checkGps(context))
        {
            //Check internet connection
            if (!Utils.checkInternetConnection(context))
            {
                DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssue.NETWORTK_NOT_AVAILABLE);
                fragment.show(getSupportFragmentManager(), "dialog");
            } else
            {
                //Check if server is reachable
                if (!Utils.checkServerAvailability())
                {
                    DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssue.SERVER_NOT_AVAILABLE);
                    fragment.show(getSupportFragmentManager(), "dialog");
                } else
                {
                    //If code reaches this line, all checks should have passed
                    //ToDo: Get known traffic issues
                    startActivity(intent);
                    finish();
                }
            }
        } else
        {
            DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssue.GPS_NOT_AVAILABLE);
            fragment.show(getSupportFragmentManager(), "dialog");
        }

    }
    @Override
    protected void onStart()
    {
        final ProgressDialog dialog = new ProgressDialog(getApplicationContext());
        dialog.setCancelable(false);
        dialog.setIndeterminate(true);
        dialog.setMessage("Some work is done here...");
        dialog.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                dialog.dismiss();
            }
        }, 5000);
    }
}
