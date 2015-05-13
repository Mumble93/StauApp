package com.sta.dhbw.stauserver.model;

import com.sta.dhbw.stauserver.db.RedisDao;

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

    public JsonObject toJsonObect()
    {
        return Json.createObjectBuilder()
                .add(RedisDao.JAM_ID, getId().toString())
                .add(RedisDao.JAM_LOCATION, Json.createObjectBuilder()
                        .add(RedisDao.JAM_LONGITUDE, getLongitude())
                        .add(RedisDao.JAM_LATITUDE, getLatitude()).build())
                .add(RedisDao.JAM_TIME, getTimestamp()).build();
    }

}
