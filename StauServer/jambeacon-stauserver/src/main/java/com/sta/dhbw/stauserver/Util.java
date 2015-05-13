package com.sta.dhbw.stauserver;


import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.TrafficJamDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util
{
    public static TrafficJamDTO trafficJamFromMap(Map<String, String> attributes)
    {
        double longitude = Double.parseDouble(attributes.get(RedisDao.JAM_LONGITUDE));
        double latitude = Double.parseDouble(attributes.get(RedisDao.JAM_LATITUDE));

        long timestamp = Long.parseLong(attributes.get(RedisDao.JAM_TIME));

        UUID id = UUID.fromString(attributes.get(RedisDao.JAM_ID));

        return new TrafficJamDTO(longitude, latitude, timestamp, id);
    }

    public static Map<String, String> trafficJamToMap(TrafficJamDTO jam)
    {
        HashMap<String, String> attributeMap = new HashMap<>();

        attributeMap.put(RedisDao.JAM_ID, jam.getId().toString());
        attributeMap.put(RedisDao.JAM_LONGITUDE, String.valueOf(jam.getLongitude()));
        attributeMap.put(RedisDao.JAM_LATITUDE, String.valueOf(jam.getLatitude()));
        attributeMap.put(RedisDao.JAM_TIME, String.valueOf(jam.getTimestamp()));

        return attributeMap;

    }
}
