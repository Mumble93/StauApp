package com.sta.dhbw.stauserver;

import android.location.Location;
import android.location.LocationManager;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.TrafficJam;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Util
{
    public static TrafficJam trafficJamFromMap(Map<String, String> attributes)
    {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLongitude(Double.parseDouble(attributes.get(RedisDao.JAM_LONGITUDE)));
        location.setLatitude(Double.parseDouble(attributes.get(RedisDao.JAM_LATITUDE)));

        long timestamp = Long.parseLong(attributes.get(RedisDao.JAM_TIME));

        UUID id = UUID.fromString(attributes.get(RedisDao.JAM_ID));

        return new TrafficJam(location, timestamp, id);
    }

    public static Map<String, String> trafficJamToMap(TrafficJam jam)
    {
        HashMap<String, String> attributeMap = new HashMap<>();

        Location location = jam.getLocation();

        attributeMap.put(RedisDao.JAM_ID, jam.getId().toString());
        attributeMap.put(RedisDao.JAM_LONGITUDE, String.valueOf(location.getLongitude()));
        attributeMap.put(RedisDao.JAM_LATITUDE, String.valueOf(location.getLatitude()));
        attributeMap.put(RedisDao.JAM_TIME, String.valueOf(jam.getTimestamp()));

        return attributeMap;

    }
}
