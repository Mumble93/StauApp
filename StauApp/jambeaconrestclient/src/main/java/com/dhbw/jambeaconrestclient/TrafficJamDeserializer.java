package com.dhbw.jambeaconrestclient;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.dhbw.jambeaconrestclient.exception.JamBeaconException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public final class TrafficJamDeserializer
{
    private static final String TAG = TrafficJamDeserializer.class.getSimpleName();

    public static TrafficJam jsonToTrafficJam(String jsonString) throws JamBeaconException
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        long timestamp;
        UUID uuid;

        try
        {
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONObject locationJson = jsonObject.getJSONObject("location");
            double longitude = locationJson.getDouble("longitude");
            double latitude = locationJson.getDouble("latitude");
            location.setLatitude(latitude);
            location.setLongitude(longitude);

            timestamp = jsonObject.getLong("timestamp");

            uuid = UUID.fromString(jsonObject.getString("id"));

        } catch (JSONException je)
        {
            Log.e(TAG, "Error deserializing response");
            throw new JamBeaconException(je.getMessage(), je);
        }

        return new TrafficJam(location, timestamp, uuid);
    }
}
