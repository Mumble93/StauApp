package com.sta.dhbw.jambeaconrestclient;

import android.location.Location;
import android.location.LocationManager;
import android.os.Parcel;
import android.os.Parcelable;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.UUID;

@JsonSerialize(using = TrafficJamSerializer.class)
@JsonDeserialize(using = TrafficJamDeserializer.class)
public class TrafficJam implements Parcelable
{
    private static final String TAG = TrafficJam.class.getSimpleName();

    private final Location location;
    private final long timestamp;
    private final UUID id;

    public TrafficJam(Location location, long timestamp, UUID id)
    {
        this.location = location;
        this.timestamp = timestamp;
        this.id = id;
    }

    public TrafficJam(Location location, long timestamp)
    {
        this(location, timestamp, UUID.randomUUID());
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

    private double[] getLocationArray()
    {
        return new double[]{location.getLongitude(), location.getLatitude()};
    }

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

    private TrafficJam(Parcel in)
    {
        this.id = UUID.fromString(in.readString());
        double[] locationArray = new double[2];
        in.readDoubleArray(locationArray);
        this.location = locationFromArray(locationArray);
        this.timestamp = in.readLong();
    }
}
