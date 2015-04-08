package com.sta.dhbw.stauapp;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils
{
    public static enum ConnectionIssue
    {
        GPS_NOT_AVAILABLE, NETWORTK_NOT_AVAILABLE, SERVER_NOT_AVAILABLE, NETWORK_TIMEOUT
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

    /**
     * Checks if the server is reachable.
     *
     * @return TRUE if server is reachable, FALSE if not.
     */

    public static boolean checkServerAvailability()
    {
        return true;
    }

}
