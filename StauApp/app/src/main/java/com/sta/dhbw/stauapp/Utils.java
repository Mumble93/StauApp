package com.sta.dhbw.stauapp;

import android.content.Context;
import android.location.LocationManager;

public class Utils
{
    public static boolean checkGps(Context context)
    {
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

}
