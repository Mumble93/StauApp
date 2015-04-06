package com.sta.dhbw.stauapp;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

public class SplashScreen extends FragmentActivity
{
    Context context;
    private static final String TAG = "Splash";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        context = getApplicationContext();

        final Intent intent = new Intent(context, MainActivity.class);
        if (Utils.checkGps(context))
        {
            //ToDo: Implement check for server connectivity and retrieval of known traffic issues
            new CountDownTimer(5000, 1000)
            {
                public void onTick(long time)
                {
                }

                public void onFinish()
                {
                    Log.i(TAG, "Closing Splash, launching app");
                    startActivity(intent);
                    finish();
                }
            }.start();

        } else
        {
            DialogFragment fragment = GpsAlertDialog.newInstance(R.string.gps_alert_dialog_title);
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
