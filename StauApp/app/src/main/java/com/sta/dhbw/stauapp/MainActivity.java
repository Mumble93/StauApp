package com.sta.dhbw.stauapp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.sta.dhbw.jambeaconrestclient.IHeartbeatCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.stauapp.dialogs.ConnectionIssueDialogFragment;
import com.sta.dhbw.stauapp.gcm.RequestGcmTokenService;
import com.sta.dhbw.stauapp.services.BeaconService;
import com.sta.dhbw.stauapp.settings.PrefFields;
import com.sta.dhbw.stauapp.settings.SettingsActivity;
import com.sta.dhbw.stauapp.util.Utils;
import com.sta.dhbw.stauapp.util.Utils.ConnectionIssue;


public class MainActivity extends Activity implements IHeartbeatCallback
{
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static final String TAG = MainActivity.class.getSimpleName();

    public static JamBeaconRestClient restClient;

    TextView mDisplay, requestIdDisplay, appVersionDisplay;
    Button jamListButton, beaconButton;
    GoogleCloudMessaging gcm;

    boolean beaconStarted = false;

    Context context;

    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        restClient = new JamBeaconRestClient();

        mDisplay = (TextView) findViewById(R.id.token_display);
        requestIdDisplay = (TextView) findViewById(R.id.request_id_display);
        appVersionDisplay = (TextView) findViewById(R.id.app_version_display);

        jamListButton = (Button) findViewById(R.id.view_traffic_issues);
        beaconButton = (Button) findViewById(R.id.start_beacon);

        context = getApplicationContext();


        /*registerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.i(TAG, "Registering");
                registerInBackground();
            }
        });*/

        jamListButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(v.getContext(), JamListActivity.class);
                startActivity(intent);
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
                registerInBackground();
            } else
            {
                Toast.makeText(this, "You are registered", Toast.LENGTH_SHORT).show();
                SharedPreferences sharedPreferences = getSharedPreferences();
                mDisplay.setText("Token: " + sharedPreferences.getString(PrefFields.PROPERTY_REG_ID, ""));
                requestIdDisplay.setText("Request Id: " + sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, ""));
                appVersionDisplay.setText("AppVersion: " + sharedPreferences.getInt(PrefFields.PROPERTY_APP_VERSION, 0));
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
                fragment.show(getFragmentManager(), "dialog");
            } else
            {
                //Check if server is reachable
                restClient.checkServerAvailability(this);
            }
        } else
        {
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.GPS_NOT_AVAILABLE);
            fragment.show(getFragmentManager(), "dialog");
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
        Toast.makeText(this, "Beacon aktiviert", Toast.LENGTH_SHORT).show();
        //ToDo: Display additional notification with icon
        Intent beaconServiceIntent = new Intent(this, BeaconService.class);
        beaconServiceIntent.putExtra(PrefFields.MIN_DISTANCE_FOR_ALERT, 2.25);
        startService(beaconServiceIntent);
        beaconButton.setText(R.string.stop_beacon_btn);
        beaconStarted = true;
    }

    private void stopBeacon()
    {
        Toast.makeText(this, "Beacon deaktiviert", Toast.LENGTH_SHORT).show();
        stopService(new Intent(this, BeaconService.class));
        beaconButton.setText(R.string.start_beacon_btn);
        beaconStarted = false;
    }

    @Override
    public void onCheckComplete(boolean success)
    {
        if (!success)
        {
            DialogFragment fragment = ConnectionIssueDialogFragment.newInstance(ConnectionIssue.SERVER_NOT_AVAILABLE);
            fragment.show(getFragmentManager(), "dialog");
        }
    }
}

