package com.sta.dhbw.stauapp.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;

import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Utils
{
    public enum ConnectionIssue
    {
        GPS_NOT_AVAILABLE, NETWORK_NOT_AVAILABLE, SERVER_NOT_AVAILABLE, NETWORK_TIMEOUT,
    }

    public enum RestIssue
    {
        GCM_REGISTRATION_AT_SERVER_FAILED
    }

    /**
     * Uses the {@code LocationManager} to determine whether the GPS Provider is enabled or not.
     *
     * @param context The application's context.
     * @return TRUE, if GPS Provider is enabled, FALSE if not.
     */
    public static boolean checkGps(Context context)
    {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Uses {@code ConnectivityManager} and {@code NetworkInfo} to check whether the device is connected to the internet or not.
     *
     * @param context The application's context.
     * @return TRUE if connected, FALSE if not.
     */
    public static boolean checkInternetConnection(Context context)
    {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }

    public class AvailabiltyCheckerTask extends AsyncTask<Context, Void, Boolean>
    {
        private JamBeaconRestClient restClient;


        @Override
        protected Boolean doInBackground(Context... params)
        {
            return null;
        }
    }
}
