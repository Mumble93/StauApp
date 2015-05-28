package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.sta.dhbw.jambeaconrestclient.util.Constants;

import java.io.IOException;


public final class TrafficJamSerializer extends JsonSerializer<TrafficJam>
{
    private static final String TAG = TrafficJamSerializer.class.getSimpleName();

    @Override
    public void serialize(TrafficJam jam, JsonGenerator gen, SerializerProvider serializers) throws IOException, JsonProcessingException
    {
        Location location = jam.getLocation();

        gen.writeStartObject();
        gen.writeNumberField(Constants.JAM_LATITUDE, location.getLatitude());
        gen.writeNumberField(Constants.JAM_LONGITUDE, location.getLongitude());
        gen.writeNumberField(Constants.JAM_TIME, jam.getTimestamp());
        gen.writeEndObject();
    }
}
