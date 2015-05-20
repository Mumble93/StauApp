package com.sta.dhbw.stauserver.db;

import com.google.gson.Gson;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.util.Constants;
import com.sta.dhbw.stauserver.util.Util;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

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
    private static final String REDIS_KEYEVENT_CHANNEL = "__keyevent@0__:expired";

    private static final String FIELD_JAM = "jam:";
    private static final String LIST_JAM = "jams";
    private static final String SET_USERS = "users";
    private static final String RECIPIENT_STRING = "recipients";
    private static final String USER_HASH_SET = "users:hashes";

    public RedisDao(String redisHost, int redisPort)
    {
        this.jedis = new Jedis(redisHost, redisPort);

        Set<String> registeredUsers = getRegisteredUsers();

        if (jedis.exists(SET_USERS) && !registeredUsers.isEmpty())
        {
            recalculateRecipients();
        }

        //The subscribe operation blocks the thread it is called on
        //So it has to be started on a new thread
        (new Thread(new ListenerRunnable(redisHost, redisPort))).start();

    }

    public RedisDao()
    {
        this("localhost", 6379);
    }

    @Override
    public TrafficJamResource getTrafficJam(String id)
    {
        Map<String, String> trafficJam = jedis.hgetAll(FIELD_JAM + id);
        if (trafficJam.isEmpty())
        {
            return null;
        }
        return Util.trafficJamFromMap(trafficJam);
    }

    @Override
    public List<TrafficJamResource> getTrafficJamList()
    {
        ArrayList<TrafficJamResource> resultList = new ArrayList<>();

        List<String> jamlist = jedis.lrange(LIST_JAM, 0, -1);
        for (String id : jamlist)
        {
            Map<String, String> attributeMap = jedis.hgetAll(FIELD_JAM + id);
            resultList.add(Util.trafficJamFromMap(attributeMap));
        }
        return resultList;
    }

    @Override
    public void storeTrafficJam(TrafficJamResource jam) throws StauserverException
    {
        String jamId = jam.getJamId().toString();

        Transaction transaction = jedis.multi();
        transaction.hmset(FIELD_JAM + jamId, Util.trafficJamToMap(jam));
        transaction.lpush(LIST_JAM, jamId);
        transaction.expire(FIELD_JAM + jamId, 600);

        List<Response<?>> responses = transaction.execGetResponse();
        for (Response<?> response : responses)
        {
            if (response.get() instanceof String)
            {
                if (!response.get().equals(REDIS_RESPONSE_OK))
                {
                    String error = "Error storing new Traffic Jam. Redis response was " + response.get();
                    throw new StauserverException(error);
                }
            }
            if (response.get() instanceof Long)
            {
                if ((Long) response.get() < 0L)
                {
                    String error = "Error storing new Traffic Jam with params: \n" + jam.toJsonObject().toString();
                    throw new StauserverException(error);
                }
            }
        }

        log.info("Created new Traffic Jam with id: " + jamId);
    }

    @Override
    public void updateTrafficJam(TrafficJamResource trafficJam)
    {
        String jamId = FIELD_JAM + trafficJam.getJamId().toString();

        Map<String, String> updatedJamValues = Util.trafficJamToMap(trafficJam);
        Map<String, String> existingJamValues = jedis.hgetAll(jamId);

        //Id and owner should not be overwritten
        existingJamValues.put(Constants.JAM_LATITUDE, updatedJamValues.get(Constants.JAM_LATITUDE));
        existingJamValues.put(Constants.JAM_LONGITUDE, updatedJamValues.get(Constants.JAM_LONGITUDE));
        existingJamValues.put(Constants.JAM_TIME, updatedJamValues.get(Constants.JAM_TIME));

        Transaction transaction = jedis.multi();

        transaction.hmset(jamId, existingJamValues);
        transaction.expire(jamId, 600);

        transaction.exec();

        log.info("Updated Traffic Jam with Id: " + jamId);
    }

    @Override
    public void deleteTrafficJam(String id) throws NotFoundException
    {
        Transaction transaction = jedis.multi();

        //Number of keys removed should be 1 in both operations
        transaction.del(FIELD_JAM + id);
        transaction.lrem(LIST_JAM, 0, id);

        List<Response<?>> responses = transaction.execGetResponse();

        for (Response response : responses)
        {
            if (response.get() instanceof Long)
            {
                if ((Long) response.get() != 1L)
                {
                    throw new NotFoundException("Deletion of Traffic Jam failed.");
                }
            }
        }

        log.info("Deleted Traffic Jam with Id: " + id);

    }

    @Override
    public Set<String> getRegisteredUsers()
    {
        return jedis.smembers(SET_USERS);
    }

    @Override
    public String getRecipientString()
    {
        if(jedis.exists(RECIPIENT_STRING))
        {
            return jedis.get(RECIPIENT_STRING);
        } else return "";

    }

    @Override
    public long createUser(String id, String hash)
    {
        Transaction transaction = jedis.multi();
        transaction.sadd(USER_HASH_SET, hash);
        transaction.sadd(SET_USERS, id);
        List<Response<?>> responses = transaction.execGetResponse();

        long result = (Long) responses.get(0).get() & (Long) responses.get(1).get();

        //User was added successfully
        if (result == 1)
        {
            recalculateRecipients();
        }

        return result;
    }

    @Override
    public void deleteUser(String id, String hash) throws NotFoundException
    {
        Transaction transaction = jedis.multi();
        transaction.srem(SET_USERS, id);
        transaction.srem(USER_HASH_SET, hash);

        List<Response<?>> responses = transaction.execGetResponse();

        if (((Long) responses.get(0).get() & (Long) responses.get(1).get()) != 1)
        {
            throw new NotFoundException("Could not delete user " + id);
        } else
        {
            recalculateRecipients();
        }
    }

    @Override
    public boolean userIsRegistered(String hash)
    {
        return jedis.sismember(USER_HASH_SET, hash);
    }

    private void recalculateRecipients()
    {
        Set<String> userSet = getRegisteredUsers();
        jedis.set(RECIPIENT_STRING, new Gson().toJson(userSet));
    }

    private class ListenerRunnable implements Runnable
    {
        private String redisHost;
        private int redisPort;

        public ListenerRunnable(String redisHost, int redisPort)
        {
            this.redisHost = redisHost;
            this.redisPort = redisPort;
        }

        @Override
        public void run()
        {
            Jedis listenerJedis = new Jedis(redisHost, redisPort);
            ExpiredListener listener = new ExpiredListener();
            listenerJedis.subscribe(listener, REDIS_KEYEVENT_CHANNEL);
        }
    }

    private class ExpiredListener extends JedisPubSub
    {
        private final Logger log = LoggerFactory.getLogger(ExpiredListener.class);

        public void onMessage(String channel, String message)
        {
            if (REDIS_KEYEVENT_CHANNEL.equals(channel))
            {
                String[] messageAttributes = message.split(":");
                String trafficJamId = messageAttributes[1];
                jedis.lrem(LIST_JAM, 0, trafficJamId);
                log.info("Jam " + trafficJamId + " expired and was removed from List");
            }
        }

        public void onSubscribe(String channel, int subscribedChannels)
        {
            log.info("Subscribed to channel " + channel);
        }

        public void onUnsubscribe(String channel, int subscribedChannels)
        {
            log.info("Unsubscribed from channel " + channel);
        }

        public void onPSubscribe(String pattern, int subscribedChannels)
        {
        }

        public void onPUnsubscribe(String pattern, int subscribedChannels)
        {
        }

        public void onPMessage(String pattern, String channel,
                               String message)
        {
        }
    }
}


