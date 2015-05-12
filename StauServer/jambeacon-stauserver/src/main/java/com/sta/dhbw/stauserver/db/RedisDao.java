package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.Util;
import com.sta.dhbw.stauserver.model.TrafficJam;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisDao
{
    private Jedis jedis;

    public static final String JAM_ID = "id";
    public static final String JAM_LONGITUDE = "long";
    public static final String JAM_LATITUDE = "lat";
    public static final String JAM_TIME = "timestamp";

    public static final String FIELD_JAM ="jam";
    public static final String LIST_JAM = "jams";
    public static final String LIST_USERS = "users";

    public RedisDao(String redisHost, int redisPort)
    {
        this.jedis = new Jedis(redisHost, redisPort);
    }

    public RedisDao()
    {
        this("localhost", 6379);
    }

    public TrafficJam getTrafficJam(String id)
    {
        Map<String, String> trafficJam = jedis.hgetAll(FIELD_JAM +id);
        return Util.trafficJamFromMap(trafficJam);
    }

    public void storeTrafficJam (TrafficJam jam)
    {
        jedis.hmset(FIELD_JAM+jam.getId().toString(), Util.trafficJamToMap(jam));
        jedis.lpush(LIST_JAM, jam.getId().toString());
    }

    public List<TrafficJam> getAllTrafficJams()
    {
        ArrayList<TrafficJam> resultList = new ArrayList<>();

        List<String> jamlist = jedis.lrange(LIST_JAM, 0, -1);
        for (String id : jamlist)
        {
            Map<String, String> attributeMap = jedis.hgetAll(FIELD_JAM+id);
            resultList.add(Util.trafficJamFromMap(attributeMap));
        }

        return resultList;
    }

    public List<String> getRegisteredUsers()
    {
        return jedis.lrange(LIST_USERS, 0, -1);
    }

    public void registerUser(String id)
    {
        jedis.lpush(LIST_USERS, id);
    }
}
