package com.sta.dhbw.stauapp.jam;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.sta.dhbw.stauapp.exception.JamBeaconException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

public final class TrafficJamDeserializer
{
    private static final String TAG = TrafficJamDeserializer.class.getSimpleName();

    public static TrafficJam jsonToTrafficJam(JSONObject jsonObject) throws JamBeaconException
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        long timestamp;
        UUID uuid;

        try
        {
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
