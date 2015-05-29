package com.sta.dhbw.stauapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.stauapp.gcm.RequestGcmTokenService;
import com.sta.dhbw.stauapp.services.BeaconService;
import com.sta.dhbw.stauapp.settings.PrefFields;
import com.sta.dhbw.stauapp.util.Utils;
import com.sta.dhbw.stauapp.util.Utils.ConnectionIssue;
import com.sta.dhbw.stauapp.dialogs.ConnectionIssueDialogFragment;
import com.sta.dhbw.stauapp.settings.SettingsActivity;

import java.io.IOException;


public class MainActivity extends AppCompatActivity
{
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String TAG = MainActivity.class.getSimpleName();

    TextView mDisplay;
    Button registerButton, jamListButton, beaconButton;
    GoogleCloudMessaging gcm;

    boolean beaconStarted = false;

    Context context;

    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisplay = (TextView) findViewById(R.id.message_display);
        registerButton = (Button) findViewById(R.id.register_user_btn);
        jamListButton = (Button) findViewById(R.id.view_traffic_issues);
        beaconButton = (Button) findViewById(R.id.start_beacon);

        context = getApplicationContext();
        

        registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
        public void onClick(View v)
            {
                Log.i(TAG, "Registering");
                registerInBackground();
            }
        });

        jamListButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //ToDo: Start Ansicht f�r Stauliste
            }
        });

        beaconButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!beaconStarted)
                {
                    MainActivity.this.startBeacon();
                } else
                {
                    MainActivity.this.stopBeacon();
                }
            }
        });

        //Check for Play Services APK. Proceed with GCM registration, if successful
        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(context);

            if (regId.isEmpty())
            {
                Log.i(TAG, "Registering for GCM Services");
                //registerInBackground();
            }
        } else
        {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }

    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (Utils.checkGps(context))
        {
            //Check internet connection
            if (!Utils.checkInternetConnection(context))
            {
                DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.NETWORK_NOT_AVAILABLE);
                fragment.show(getSupportFragmentManager(), "dialog");
            } else
            {
                //Check if server is reachable
                if (!Utils.checkServerAvailability())
                {
                    DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.SERVER_NOT_AVAILABLE);
                    fragment.show(getSupportFragmentManager(), "dialog");
                }
            }
        } else
        {
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.GPS_NOT_AVAILABLE);
            fragment.show(getSupportFragmentManager(), "dialog");
        }

        checkPlayServices();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    /**
     * Checks if a valid Google Play Services APK is available
     *
     * @return TRUE if APK is available, FALSE otherwise
     */
    private boolean checkPlayServices()
    {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS)
        {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
            {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else
            {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        Log.i(TAG, "Google Play Services available");
        return true;
    }


    /**
     * Get the current GCM registration ID
     * <p/>
     * If the returned String is empty, the device needs to register.
     *
     * @param context The application context
     * @return The registration ID, if it exists. If not, an empty String is returned.
     */
    private String getRegistrationId(Context context)
    {
        final SharedPreferences prefs = getSharedPreferences();
        String registrationId = prefs.getString(PrefFields.PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId.isEmpty())
        {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        //Check for app updates
        int registeredVersion = prefs.getInt(PrefFields.PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion)
        {
            Log.i(TAG, "App version changed");
            return "";
        }
        return registrationId;
    }

    /**
     * @return The application's {@code SharedPreferences}
     */
    private SharedPreferences getSharedPreferences()
    {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context)
    {
        try
        {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e)
        {
            //should never happen. If so, your programming is bad, and you should feel bad.
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private void registerInBackground()
    {
        Log.i(TAG, "Getting new Registration Id");

        SharedPreferences sharedPreferences = getSharedPreferences();

        Intent intent = new Intent(this, RequestGcmTokenService.class);
        startService(intent);

        sharedPreferences.edit().putInt(PrefFields.PROPERTY_APP_VERSION, getAppVersion(context)).apply();
    }

    private void startBeacon()
    {
        Toast.makeText(context, "Beacon aktiviert", Toast.LENGTH_SHORT).show();
        //ToDo: Display additional notification with icon
        Intent beaconServiceIntent = new Intent(context, BeaconService.class);
        beaconServiceIntent.putExtra(PrefFields.MIN_DISTANCE_FOR_ALERT, 2.25);
        startService(beaconServiceIntent);
        beaconButton.setText(R.string.stop_beacon_btn);
        beaconStarted = true;
    }

    private void stopBeacon()
    {
        Toast.makeText(context, "Beacon deaktiviert", Toast.LENGTH_SHORT).show();
        stopService(new Intent(context, BeaconService.class));
        beaconButton.setText(R.string.start_beacon_btn);
        beaconStarted = false;
    }
}

