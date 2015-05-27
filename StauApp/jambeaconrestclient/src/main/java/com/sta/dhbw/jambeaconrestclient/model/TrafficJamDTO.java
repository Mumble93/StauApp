package com.sta.dhbw.jambeaconrestclient.model;

import java.io.Serializable;
import java.util.UUID;

public class TrafficJamDTO implements Serializable
{
    private double longitude, latitude;
    private long timestamp;
    private UUID id;

    public TrafficJamDTO(double longitude, double latitude, long timestamp, UUID id)
    {

        this.longitude = longitude;
        this.latitude = latitude;
        this.timestamp = timestamp;
        this.id = id;
    }

    public TrafficJamDTO(){}

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

    public UUID getId(){return id;}

    public void setId(UUID id)
    {
        this.id = id;
    }
    }
