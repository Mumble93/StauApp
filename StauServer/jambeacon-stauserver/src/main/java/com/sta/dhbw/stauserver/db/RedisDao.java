package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.util.Util;
import com.sta.dhbw.stauserver.model.TrafficJamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisDao
{
    private static final Logger log = LoggerFactory.getLogger(RedisDao.class);

    private Jedis jedis;

    private static final String FIELD_JAM ="jam:";
    private static final String LIST_JAM = "jams";
    private static final String LIST_USERS = "users";

    public RedisDao(String redisHost, int redisPort)
    {
        this.jedis = new Jedis(redisHost, redisPort);
    }

    public RedisDao()
    {
        this("localhost", 6379);
    }

    public TrafficJamDTO getTrafficJam(String id)
    {
        Map<String, String> trafficJam = jedis.hgetAll(FIELD_JAM +id);
        return Util.trafficJamFromMap(trafficJam);
    }

    public void storeTrafficJam (TrafficJamDTO jam)
    {
        String jamId = jam.getId().toString();

        jedis.hmset(FIELD_JAM+jamId, Util.trafficJamToMap(jam));
        jedis.lpush(LIST_JAM, jamId);
    }

    public List<TrafficJamDTO> getAllTrafficJams()
    {
        ArrayList<TrafficJamDTO> resultList = new ArrayList<>();

        List<String> jamlist = jedis.lrange(LIST_JAM, 0, -1);
        for (String id : jamlist)
        {
            Map<String, String> attributeMap = jedis.hgetAll(FIELD_JAM+id);
            resultList.add(Util.trafficJamFromMap(attributeMap));
        }
        return resultList;
    }

    public void deleteTrafficJam(String id)
    {
        jedis.del(FIELD_JAM+id);
        jedis.lrem(LIST_JAM, 0, id);
    }

    public List<String> getRegisteredUsers()
    {
        return jedis.lrange(LIST_USERS, 0, -1);
    }

    public long createUser(String id)
    {
         return jedis.lpush(LIST_USERS, id);
    }

    public long deleteUser(String id)
    {
        return jedis.lrem(LIST_USERS, 0, id);
    }
}
