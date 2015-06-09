package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.sta.dhbw.jambeaconrestclient.util.Constants;

import java.util.UUID;

/**
 * This class represents a traffic jam resource on the server side and is sent to the
 * jam endpoint, when a traffic jam has been detected by the BeaconService.
 */
@JsonSerialize(using = TrafficJamSerializer.class)
@JsonDeserialize(using = TrafficJamDeserializer.class)
public class TrafficJam implements Parcelable
{
    private static final String TAG = TrafficJam.class.getSimpleName();

    private final Location location;
    private final long timestamp;
    @JsonProperty(Constants.JAM_ID)
    private UUID id;

    public TrafficJam(Location location, long timestamp, UUID id)
    {
        this.location = location;
        this.timestamp = timestamp;
        if (null != id)
        {
            this.id = id;
        }
    }

    /**
     * Returns a TrafficJam without Id.
     *
     * @param location  The {@code Location} of the jam.
     * @param timestamp The timestamp of detection, as long.
     */
    public TrafficJam(Location location, long timestamp)
    {
        this(location, timestamp, null);
    }

    public Location getLocation()
    {
        return location;
    }

    public long getTimestamp()
    {
        return timestamp;
    }

    public UUID getId()
    {
        return id;
    }

    /**
     * @return A double array, containing longitude and latitude of the location.
     */
    private double[] getLocationArray()
    {
        return new double[]{location.getLongitude(), location.getLatitude()};
    }

    /**
     * @param locationArray A double array, containing longitude and latitude.
     * @return A {@code Location} object, with the provider set to the LocationManager.GPS_PROVIDER
     */
    private Location locationFromArray(double[] locationArray)
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLongitude(locationArray[0]);
        location.setLatitude(locationArray[1]);
        return location;
    }

    @Override
    public int describeContents()
    {
        //Ignore
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeString(id.toString());
        dest.writeDoubleArray(getLocationArray());
        dest.writeLong(timestamp);
    }

    public static final Creator<TrafficJam> CREATOR = new Creator<TrafficJam>()
    {
        public TrafficJam createFromParcel(Parcel in)
        {
            return new TrafficJam(in);
        }

        public TrafficJam[] newArray(int size)
        {
            return new TrafficJam[size];
        }
    };

    /**
     * Creates a TrafficJam object from a {@code Parcel}.
     *
     * @param in The Parcel from which to create the TrafficJam.
     */
    private TrafficJam(Parcel in)
    {
        this.id = UUID.fromString(in.readString());
        double[] locationArray = new double[2];
        in.readDoubleArray(locationArray);
        this.location = locationFromArray(locationArray);
        this.timestamp = in.readLong();
    }
}
