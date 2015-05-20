package com.sta.dhbw.stauserver.resource;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sta.dhbw.stauserver.util.Constants;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.Serializable;
import java.util.UUID;

@JsonIgnoreProperties({"owner"})
public class TrafficJamResource implements Serializable
{
    private double longitude, latitude;
    private long timestamp;
    private UUID jamId;
    private String owner;

    public TrafficJamResource(double longitude, double latitude, long timestamp, UUID jamId, String owner)
    {

        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.jamId = jamId;
        this.owner = owner;
    }

    public TrafficJamResource(double longitude, double latitude, long timestamp, String owner)
    {
        this(longitude, latitude, timestamp, UUID.randomUUID(), owner);
    }

    public TrafficJamResource(){}

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

    public UUID getJamId()
    {
        return jamId;
    }

    public void setJamId(UUID jamId)
    {
        this.jamId = jamId;
    }

    public String getOwner()

    {
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    public JsonObject toJsonObject()
    {
        return Json.createObjectBuilder()
                .add(Constants.JAM_ID, getJamId().toString())
                .add(Constants.JAM_LOCATION, Json.createObjectBuilder()
                        .add(Constants.JAM_LONGITUDE, getLongitude())
                        .add(Constants.JAM_LATITUDE, getLatitude()).build())
                .add(Constants.JAM_TIME, getTimestamp()).build();
    }

}
