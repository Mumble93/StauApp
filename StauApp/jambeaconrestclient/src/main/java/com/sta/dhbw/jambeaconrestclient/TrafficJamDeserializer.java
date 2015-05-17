package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;
import android.location.LocationManager;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.jambeaconrestclient.model.TrafficJamDTO;

public final class TrafficJamDeserializer
{
    private static final String TAG = TrafficJamDeserializer.class.getSimpleName();

    public static TrafficJam dtoToTrafficJam(TrafficJamDTO dto) throws JamBeaconException
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(dto.getLatitude());
        location.setLongitude(dto.getLongitude());

        return new TrafficJam(location, dto.getTimestamp(), dto.getId());
    }
}
