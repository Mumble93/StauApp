package com.sta.dhbw.stauapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.sta.dhbw.stauapp.Utils.ConnectionIssues;

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
                DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssues.NETWORTK_NOT_AVAILABLE);
                fragment.show(getSupportFragmentManager(), "dialog");
            } else
            {
                //Check if server is reachable
                if (!Utils.checkServerAvailability())
                {
                    DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssues.SERVER_NOT_AVAILABLE);
                    fragment.show(getSupportFragmentManager(), "dialog");
                } else
                {
                    //If code reaches this line, all tests should have passed
                    //ToDo: Get known traffic issues
                    startActivity(intent);
                    finish();
                }
            }
        } else
        {
            DialogFragment fragment = AlertDialogFragment.newInstance(ConnectionIssues.GPS_NOT_AVAILABLE);
            fragment.show(getSupportFragmentManager(), "dialog");
        }

    }


    private class ConnectionTester extends AsyncTask<Void, Void, Boolean>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            super.onPostExecute(result);
        }
    }
}
