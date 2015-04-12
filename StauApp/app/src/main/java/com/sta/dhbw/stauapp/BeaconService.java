package com.sta.dhbw.stauapp;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class BeaconService extends Service
{
    /**
     * This class is subject to change. Remodeling to IntentService class in near future possible.
     */
    public static final String TAG = BeaconService.class.getSimpleName();

    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private static Location lastLocation;

    private static double minDistance;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int id)
    {
        Log.i(TAG, "Beacon Service was started.");
        minDistance = intent.getDoubleExtra(MainActivity.MIN_DISTANCE_FOR_ALERT, 2.25);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, locationListener);
        return super.onStartCommand(intent, flags, id);
    }


    @Override
    public void onCreate()
    {
        Log.i(TAG, "Beacon Service created");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                if (null == lastLocation)
                {
                    lastLocation = location;
                    return;
                } else if (drivenDistanceBelowMinimum(lastLocation, location))
                {
                    Log.i(TAG, "Detected traffic jam at " + location.getLatitude() + " " + location.getLongitude());
                    sendAlarmToServer();
                }

                Log.i(TAG, "Got new position: " + location.getLatitude() + " " + location.getLongitude());

                lastLocation = location;

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {
                if (LocationManager.GPS_PROVIDER.equals(s))
                {
                    switch (i)
                    {
                        case LocationProvider.AVAILABLE:
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
                            Log.i(TAG, "Location Provider became available!");
                            break;
                        default:
                            locationManager.removeUpdates(locationListener);
                            Log.i(TAG, "Location Manager became unavailable!");
                            break;
                    }
                }
            }

            @Override
            public void onProviderEnabled(String s)
            {
                if (LocationManager.GPS_PROVIDER.equals(s))
                {
                    Log.i(TAG, "Provider enabled while service running");
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
                }

            }

            @Override
            public void onProviderDisabled(String s)
            {
                if (LocationManager.GPS_PROVIDER.equals(s))
                {
                    Log.i(TAG, "Provider disabled while service running");
                    locationManager.removeUpdates(locationListener);
                }
            }
        };
    }

    @Override
    public void onDestroy()
    {
        locationManager.removeUpdates(locationListener);
        Log.i(TAG, "Beacon Service destroyed");
    }

    private boolean drivenDistanceBelowMinimum(Location lastLocation, Location currentLocation)
    {
        float results[] = new float[1];

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude(), results);

        return results[0] < minDistance;
    }

    private void sendAlarmToServer()
    {
        //ToDo: Implement connecting to server and sending alert
    }
}
