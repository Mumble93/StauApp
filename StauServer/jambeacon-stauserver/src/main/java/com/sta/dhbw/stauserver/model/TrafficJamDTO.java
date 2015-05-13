package com.sta.dhbw.stauserver.model;

import com.sta.dhbw.stauserver.util.Constants;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.UUID;

public class TrafficJamDTO implements Serializable
{
    private double longitude, latitude;
    private long timestamp;
    private final UUID id;

    public TrafficJamDTO(double longitude, double latitude, long timestamp, UUID id)
    {

        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.id = id;
    }

    public TrafficJamDTO(double longitude, double latitude, long timestamp)
    {
        this(longitude, latitude, timestamp, UUID.randomUUID());
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public void setTimestamp(long timestamp)
    {
        this.timestamp = timestamp;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public UUID getId()
    {
        return id;
    }

    public JsonObject toJsonObject()
    {
        return Json.createObjectBuilder()
                .add(Constants.JAM_ID, getId().toString())
                .add(Constants.JAM_LOCATION, Json.createObjectBuilder()
                        .add(Constants.JAM_LONGITUDE, getLongitude())
                        .add(Constants.JAM_LATITUDE, getLatitude()).build())
                .add(Constants.JAM_TIME, getTimestamp()).build();
    }

}
