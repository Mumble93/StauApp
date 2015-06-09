package com.sta.dhbw.stauapp.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.sta.dhbw.jambeaconrestclient.ITrafficJamCallback;
import com.sta.dhbw.jambeaconrestclient.JamBeaconRestClient;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;
import com.sta.dhbw.stauapp.MainActivity;
import com.sta.dhbw.stauapp.settings.PrefFields;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BeaconService extends Service implements ITrafficJamCallback
{

    private static final String TAG = BeaconService.class.getSimpleName();

    private static SharedPreferences sharedPreferences;

    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private static Location lastLocation;

    private static final double minDistance = 2.25;
    private static int delay;
    private static boolean beaconHasStarted = false;

    private JamBeaconRestClient restClient;

    private static TrafficJam reportedTrafficJam;

    private ITrafficJamCallback caller = this;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


    /**
     * Waits for a specified delay in minutes or the first time the user is faster than 65 km/h to start the
     * traffic jam detection. When detection started, location will be polled every 3 minutes.
     * {@inheritDoc}
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int id)
    {
        Log.i(TAG, "Beacon Service was started.");

        int delay = Integer.parseInt(sharedPreferences.getString("beacon_delay", "3"));

        Log.d(TAG, "Starting Beacon with " + delay + " minutes delay");

        if (delay == 0)
        {
            //Start immediately
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
            beaconHasStarted = true;
            sendBroadcast(new Intent().setAction(MainActivity.BeaconBroadcastReceiver.BEACON_STARTET));
        } else
        {
            //Check location every minute to determine speed
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);
        }
        return super.onStartCommand(intent, flags, id);
    }


    @Override
    public void onCreate()
    {
        Log.i(TAG, "Beacon Service created");

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        delay = Integer.parseInt(sharedPreferences.getString("beacon_delay", "3"));

        locationListener = new BeaconLocationListener();

        restClient = new JamBeaconRestClient();
    }

    @Override
    public void onDestroy()
    {
        locationManager.removeUpdates(locationListener);
        beaconHasStarted = false;
        Log.i(TAG, "Beacon Service destroyed");
    }


    /**
     * Custom {@code LocationListener}, that will check if the driven distance between two Locations
     * is below a certain value. If so, a new TrafficJam will be sent to the server.
     */
    private class BeaconLocationListener implements LocationListener
    {
        private int delayCounter = 0;

        @Override
        public void onLocationChanged(Location location)
        {
            if (!beaconHasStarted)
            {
                //If the detection has not started check the speed
                if (location.hasSpeed())
                {
                    //Speed is returned in meter/second
                    float speed = location.getSpeed() * 3.6f;
                    if (speed >= 65.0f)
                    {
                        beaconHasStarted = true;
                        lastLocation = location;
                        locationManager.removeUpdates(locationListener);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
                        sendBroadcast(new Intent().setAction(MainActivity.BeaconBroadcastReceiver.BEACON_STARTET));
                        return;
                    }
                }
                if (delayCounter != delay)
                {
                    delayCounter++;
                    return;
                } else
                {
                    beaconHasStarted = true;
                    lastLocation = location;
                    locationManager.removeUpdates(locationListener);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, locationListener);
                    sendBroadcast(new Intent().setAction(MainActivity.BeaconBroadcastReceiver.BEACON_STARTET));
                    return;
                }
            }

            if (null == lastLocation)
            {
                lastLocation = location;
                return;
            } else if (drivenDistanceBelowMinimum(lastLocation, location))
            {
                Date date = new Date();

                String msg = "Detected traffic jam at " + location.getLatitude() + " " + location.getLongitude();
                msg += "on " + DateFormat.getTimeInstance().format(date);

                Log.d(TAG, msg);
                if (reportedTrafficJam == null)
                {
                    reportedTrafficJam = new TrafficJam(location, date.getTime());
                    restClient.postTrafficJam(reportedTrafficJam, sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, ""), caller);
                } else
                {
                    UUID id = reportedTrafficJam.getId();
                    reportedTrafficJam = new TrafficJam(location, date.getTime(), id);
                    restClient.updateTrafficJam(reportedTrafficJam, sharedPreferences.getString(PrefFields.PROPERTY_X_REQUEST_ID, ""), caller);
                }
            }

            Log.d(TAG, "Got new position: " + location.getLatitude() + " " + location.getLongitude());

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
                        sendBroadcast(new Intent().setAction(MainActivity.BeaconBroadcastReceiver.BEACON_STARTET));
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
                sendBroadcast(new Intent().setAction(MainActivity.BeaconBroadcastReceiver.BEACON_STARTET));
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
    }

    /**
     * Checks if the distance between two locations is below the minimum value.<br>
     * Default minimum distance is 2.25km, which implies an average speed of 45km/h.
     *
     * @param lastLocation    The last measured Location.
     * @param currentLocation The current Location.
     * @return True if the distance between both locations is below minimum, False otherwise.
     */
    protected boolean drivenDistanceBelowMinimum(Location lastLocation, Location currentLocation)
    {
        float results[] = new float[1];

        Location.distanceBetween(lastLocation.getLatitude(), lastLocation.getLongitude(),
                currentLocation.getLatitude(), currentLocation.getLongitude(), results);

        return results[0] < minDistance;
    }

    @Override
    public void onGetTrafficJamComplete(TrafficJam trafficJam)
    {

    }

    @Override
    public void onGetJamListComplete(List<TrafficJam> trafficJamList)
    {

    }

    @Override
    public void onTrafficJamUpdateComplete(TrafficJam updatedJam)
    {

    }

    @Override
    public void onTrafficJamPostComplete(TrafficJam jam)
    {

    }
}
