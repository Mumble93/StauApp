package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;
import android.location.LocationManager;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import com.sta.dhbw.jambeaconrestclient.util.Constants;

import java.io.IOException;
import java.util.UUID;

public final class TrafficJamDeserializer extends JsonDeserializer<TrafficJam>
{
    private static final String TAG = TrafficJamDeserializer.class.getSimpleName();

    @Override
    public TrafficJam deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);

        JsonNode jsonNode = jp.getCodec().readTree(jp);

        UUID jamId = UUID.fromString(jsonNode.get(Constants.JAM_ID).asText());
        double latitude = (Double) jsonNode.get(Constants.JAM_LATITUDE).numberValue();
        double longitude = (Double) jsonNode.get(Constants.JAM_LONGITUDE).numberValue();
        long timestamp = (Long) jsonNode.get(Constants.JAM_TIME).numberValue();

        location.setLatitude(latitude);
        location.setLongitude(longitude);

        return new TrafficJam(location, timestamp, jamId);
    }
}
