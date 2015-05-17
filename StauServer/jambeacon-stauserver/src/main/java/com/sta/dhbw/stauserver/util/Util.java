package com.sta.dhbw.stauserver.util;


import com.sta.dhbw.stauserver.model.TrafficJamModel;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Util
{
    public static boolean validateUuId(UUID id)
    {
        return validateUuId(id.toString());
    }

    public static boolean validateUuId(String id)
    {
        String validUUIDRegex = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$";
        return id.matches(validUUIDRegex);
    }

    public static TrafficJamModel trafficJamFromMap(Map<String, String> attributes)
    {
        double longitude = Double.parseDouble(attributes.get(Constants.JAM_LONGITUDE));
        double latitude = Double.parseDouble(attributes.get(Constants.JAM_LATITUDE));

        long timestamp = Long.parseLong(attributes.get(Constants.JAM_TIME));

        UUID id = UUID.fromString(attributes.get(Constants.JAM_ID));

        String owner = attributes.get(attributes.get(Constants.JAM_OWNER));

        return new TrafficJamModel(longitude, latitude, timestamp, id, owner);
    }

    public static Map<String, String> trafficJamToMap(TrafficJamModel jam)
    {
        HashMap<String, String> attributeMap = new HashMap<>();

        attributeMap.put(Constants.JAM_ID, jam.getJamId().toString());
        attributeMap.put(Constants.JAM_OWNER, jam.getOwner());
        attributeMap.put(Constants.JAM_LONGITUDE, String.valueOf(jam.getLongitude()));
        attributeMap.put(Constants.JAM_LATITUDE, String.valueOf(jam.getLatitude()));
        attributeMap.put(Constants.JAM_TIME, String.valueOf(jam.getTimestamp()));

        return attributeMap;

    }
}
