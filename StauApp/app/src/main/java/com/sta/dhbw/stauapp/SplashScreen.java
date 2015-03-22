package com.sta.dhbw.stauapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

public class SplashScreen extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(SplashScreen.this, MainActivity.class);
        if (Utils.checkGps(SplashScreen.this))
        {
            //ToDo: Implement check for server connectivity and retrieval of known traffic issues
            try
            {
                Thread.sleep(3000L);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            startActivity(intent);
            finish();
        } else
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.app_name);
            builder.setCancelable(false);
            builder.setMessage(R.string.gps_alert_message);
            builder.setPositiveButton(R.string.dialog_close, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                    finish();
                }
            });
            Dialog dialog = builder.create();
            dialog.show();
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
