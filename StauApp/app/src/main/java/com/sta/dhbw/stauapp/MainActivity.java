package com.sta.dhbw.stauapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.sta.dhbw.stauapp.settings.SettingsActivity;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends ActionBarActivity
{
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";

    private static final String TAG = "JamBeacon";

    String SENDER_ID = "821661182636";

    TextView mDisplay;
    Button routeButton, jamListButton;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId = new AtomicInteger();
    SharedPreferences prefs;

    Context context;

    String regId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisplay = (TextView) findViewById(R.id.message_display);
        routeButton = (Button) findViewById(R.id.new_route);
        jamListButton = (Button) findViewById(R.id.view_traffic_issues);

        routeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //ToDo: Starte Routeneingabefenster und Standorterfassung
            }
        });

        jamListButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //ToDo: Start Ansicht für Stauliste
            }
        });

        context = getApplicationContext();

        //Check for Play Services APK. Proceed with GCM registration, if successful
        if (checkPlayServices())
        {
            gcm = GoogleCloudMessaging.getInstance(this);
            regId = getRegistrationId(context);

            if (regId.isEmpty())
            {
                Log.i(TAG, "Registering for GCM Services");
                registerInBackground();
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
        if (!Utils.checkGps(context))
        {
            DialogFragment fragment = GpsAlertDialog.newInstance(R.string.gps_alert_dialog_title);
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
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId == null || registrationId.isEmpty())
        {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        //Check for app updates
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
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
    private SharedPreferences getGCMPreferences(Context context)
    {
        return getSharedPreferences(MainActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
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
        new AsyncTask<Void, Void, String>()
        {
            @Override
            protected String doInBackground(Void... params)
            {
                String msg = "";
                try
                {
                    if (gcm == null)
                    {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID = " + regId;
                    //Send Id to server, so server can send messages to app
                    sendRegistrationIdToBackend();
                    //Persist registration Id
                    storeRegistrationId(context, regId);
                } catch (IOException ex)
                {
                    msg = "Error: " + ex.getMessage();
                    //ToDo: Implement mechanism to prompt user to re-register
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg)
            {
                //ToDo: For development only. DELETE BEFORE RELEASE!
                mDisplay.append(msg + "\n");
            }
        }.execute(null, null, null);
    }

    /**
     * Send registration Id to application server, so server can send messages to device.
     */
    private void sendRegistrationIdToBackend()
    {
        //ToDo: I don't think it is necessary to do this, still keeping it here just in case.
    }

    /**
     * Stores the registration Id and appVersion Code in {@code SharedPreferences}.
     *
     * @param context The application's context
     * @param regId   registration Id
     */
    private void storeRegistrationId(Context context, String regId)
    {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }
}

