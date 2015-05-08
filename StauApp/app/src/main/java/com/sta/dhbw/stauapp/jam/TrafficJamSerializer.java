package com.sta.dhbw.stauapp.jam;

import android.util.Log;

import com.sta.dhbw.stauapp.exception.JamBeaconException;

import org.json.JSONException;
import org.json.JSONObject;

public final class TrafficJamSerializer
{
    private static final String TAG = TrafficJamSerializer.class.getSimpleName();

    public static JSONObject trafficJamToJson(TrafficJam trafficJam) throws JamBeaconException
    {
        JSONObject jamJson = new JSONObject();
        JSONObject locationObject = new JSONObject();
        try
        {
            locationObject.put("longitude", trafficJam.getLocation().getLongitude());
            locationObject.put("latitude", trafficJam.getLocation().getLatitude());

            jamJson.put("location",locationObject);
            jamJson.put("timestamp", trafficJam.getTimestamp());
            jamJson.put("id", trafficJam.getId().toString());
        }catch (JSONException je)
        {
            Log.e(TAG, "Error serializing jam");
            throw new JamBeaconException(je.getMessage(), je);
        }

        return jamJson;
    }
}
