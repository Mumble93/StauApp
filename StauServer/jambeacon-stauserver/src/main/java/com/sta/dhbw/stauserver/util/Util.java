package com.sta.dhbw.stauserver.util;


import com.sta.dhbw.stauserver.model.TrafficJamDTO;

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

    public static TrafficJamDTO trafficJamFromMap(Map<String, String> attributes)
    {
        double longitude = Double.parseDouble(attributes.get(Constants.JAM_LONGITUDE));
        double latitude = Double.parseDouble(attributes.get(Constants.JAM_LATITUDE));

        long timestamp = Long.parseLong(attributes.get(Constants.JAM_TIME));

        UUID id = UUID.fromString(attributes.get(Constants.JAM_ID));

        return new TrafficJamDTO(longitude, latitude, timestamp, id);
    }

    public static Map<String, String> trafficJamToMap(TrafficJamDTO jam)
    {
        HashMap<String, String> attributeMap = new HashMap<>();

        attributeMap.put(Constants.JAM_ID, jam.getId().toString());
        attributeMap.put(Constants.JAM_LONGITUDE, String.valueOf(jam.getLongitude()));
        attributeMap.put(Constants.JAM_LATITUDE, String.valueOf(jam.getLatitude()));
        attributeMap.put(Constants.JAM_TIME, String.valueOf(jam.getTimestamp()));

        return attributeMap;

    }
}
