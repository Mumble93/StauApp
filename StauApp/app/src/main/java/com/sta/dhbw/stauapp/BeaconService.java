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
    public static final String TAG = BeaconService.class.getSimpleName();

    private LocationManager locationManager;
    private LocationListener locationListener;

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
                } else if (drivenDistanceBelowMinimum(lastLocation, location))
                {
                    sendAlarmToServer();
                }

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
                            break;
                        default:
                            locationManager.removeUpdates(locationListener);
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
