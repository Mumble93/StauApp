package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.jambeaconrestclient.model.TrafficJamDTO;


public final class TrafficJamSerializer
{
    private static final String TAG = TrafficJamSerializer.class.getSimpleName();

    public static TrafficJamDTO trafficJamToDto(TrafficJam trafficJam) throws JamBeaconException
    {
        Location location = trafficJam.getLocation();
        return new TrafficJamDTO(location.getLongitude(), location.getLatitude(), trafficJam.getTimestamp(), trafficJam.getId());
    }
}
