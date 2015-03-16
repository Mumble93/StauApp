package com.sta.dhbw.stauapp;

import android.location.LocationManager;

public class Utils
{
    public static boolean checkGps(LocationManager service)
    {
        return service.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
