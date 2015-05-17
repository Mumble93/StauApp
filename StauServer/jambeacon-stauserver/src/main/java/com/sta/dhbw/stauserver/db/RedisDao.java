package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.util.Constants;
import com.sta.dhbw.stauserver.util.Util;
import com.sta.dhbw.stauserver.model.TrafficJamModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisDao implements IBeaconDb
{
    private static final Logger log = LoggerFactory.getLogger(RedisDao.class);

    private Jedis jedis;

    private static final String REDIS_RESPONSE_OK = "OK";

    private static final String FIELD_JAM = "jam:";
    private static final String LIST_JAM = "jams";
    private static final String SET_USERS = "users";
    private static final String USER_HASH = "user:%s:hash";

    public RedisDao(String redisHost, int redisPort)
    {
        this.jedis = new Jedis(redisHost, redisPort);
    }

    public RedisDao()
    {
        this("localhost", 6379);
    }

    @Override
    public TrafficJamModel getTrafficJam(String id) throws NotFoundException
    {
        Map<String, String> trafficJam = jedis.hgetAll(FIELD_JAM + id);
        if (trafficJam.isEmpty())
        {
            String error = "Error retrieving Traffic Jam with Id " + id;
            throw new NotFoundException(error);
        }
        return Util.trafficJamFromMap(trafficJam);
    }

    @Override
    public List<TrafficJamModel> getTrafficJamList()
    {
        ArrayList<TrafficJamModel> resultList = new ArrayList<>();

        List<String> jamlist = jedis.lrange(LIST_JAM, 0, -1);
        for (String id : jamlist)
        {
            Map<String, String> attributeMap = jedis.hgetAll(FIELD_JAM + id);
            resultList.add(Util.trafficJamFromMap(attributeMap));
        }
        return resultList;
    }

    @Override
    public void storeTrafficJam(TrafficJamModel jam) throws StauserverException
    {
        String jamId = jam.getJamId().toString();

        String hashSet = jedis.hmset(FIELD_JAM + jamId, Util.trafficJamToMap(jam));
        if (hashSet.equals(REDIS_RESPONSE_OK))
        {
            //Redis replies with the number of elements in the list after push operation
            //Therefore, result of push operation should never be less zero
            long pushResponse = jedis.lpush(LIST_JAM, jamId);
            if(pushResponse<0)
            {
                String error = "Error storing new Traffic Jam with params: \n" + jam.toJsonObject().toString();
                throw new StauserverException(error);
            }
        } else
        {
            String error = "Error storing new Traffic Jam. Redis response was " + hashSet;
            throw new StauserverException(error);
        }

        log.info("Created new Traffic Jam with id: " + jamId);
    }

    @Override
    public void updateTrafficJam(TrafficJamModel trafficJam)
    {
        String jamId = FIELD_JAM + trafficJam.getJamId().toString();

        Map<String, String> updatedJamValues = Util.trafficJamToMap(trafficJam);
        Map<String, String> existingJamValues = jedis.hgetAll(jamId);

        //Id and owner should not be overwritten
        existingJamValues.put(Constants.JAM_LATITUDE, updatedJamValues.get(Constants.JAM_LATITUDE));
        existingJamValues.put(Constants.JAM_LONGITUDE, updatedJamValues.get(Constants.JAM_LONGITUDE));
        existingJamValues.put(Constants.JAM_TIME, updatedJamValues.get(Constants.JAM_TIME));

        jedis.hmset(jamId, existingJamValues);

        log.info("Updated Traffic Jam with Id: " + jamId);
    }

    @Override
    public void deleteTrafficJam(String id) throws NotFoundException
    {
        //Number of keys removed should be 1 in both operations
        long deleteJamResult = jedis.del(FIELD_JAM + id);
        long deleteJamFromListResult = jedis.lrem(LIST_JAM, 0, id);

        if(deleteJamFromListResult != 1 && deleteJamResult != 1)
        {
            throw new NotFoundException("Deletion of Traffic Jam failed.");
        }

        log.info("Deleted Traffic Jam with Id: " + id);

    }

    @Override
    public Set<String> getRegisteredUsers()
    {
        return jedis.smembers(SET_USERS);
    }

    @Override
    public long createUser(String id, String hash)
    {
        jedis.set(String.format(USER_HASH, id), hash);
        return jedis.sadd(SET_USERS, id);
    }

    @Override
    public void deleteUser(String id, String hash) throws NotFoundException
    {
        if(jedis.srem(SET_USERS, id) != 1 && jedis.del(String.format(USER_HASH, id)) != 1)
        {
            throw new NotFoundException("Could not delete user " + id);
        }
    }
}
