package com.sta.dhbw.stauapp.util;

import android.content.Context;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.text.DateFormat;
import java.util.Date;

/**
 * Multi purpose utility class.
 */
public class Utils
{
    /**
     * Enum to represent different issues that can arise while using the app, such as
     * unavailable network, server or deactivated GPS.
     */
    public enum ConnectionIssue
    {
        GPS_NOT_AVAILABLE, NETWORK_NOT_AVAILABLE, SERVER_NOT_AVAILABLE, NETWORK_TIMEOUT,
    }

    /**
     * Turns a timestamp into String representation in the format of HH:mm:ss
     *
     * @param timestamp The timestamp as long.
     * @return The timestamp formatted as HH:mm:ss String.
     */
    public static String timstampToString(long timestamp)
    {
        DateFormat dateFormat = DateFormat.getTimeInstance();
        Date date = new Date(timestamp);
        return dateFormat.format(date);
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
}
