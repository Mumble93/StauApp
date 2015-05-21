package com.sta.dhbw.stauserver.db;

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

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.NotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Singleton
@Startup
public class RedisDao implements IBeaconDb
{
    private static final Logger log = LoggerFactory.getLogger(RedisDao.class);

    private Jedis jedis;

    @Resource
    private static ManagedThreadFactory threadFactory;

    private static final String REDIS_RESPONSE_OK = "OK";
    private static final String REDIS_KEYEVENT_CHANNEL = "__keyevent@0__:expired";

    private static final String FIELD_JAM = "jam:";
    private static final String LIST_JAM = "jams";
    private static final String LIST_USERS = "users";
    private static final String USER_HASH_SET = "users:hashes";

    public RedisDao(String redisHost, int redisPort) throws StauserverException
    {
        try
        {
            if (null == threadFactory)
            {
                synchronized (RedisDao.class)
                {
                    if (null == threadFactory)
                    {
                        threadFactory = InitialContext.doLookup("java:comp/DefaultManagedThreadFactory");
                        log.info("Successfully got ThreadFactory");
                    }
                }
            }
        } catch (NamingException e)
        {
            String error = "Error getting ThreadFactory. " + e.getMessage();
            throw new StauserverException(error, e);
        }

        this.jedis = new Jedis(redisHost, redisPort);

        //The subscribe operation blocks the thread it is called on
        //So it has to be started on a new thread and Jedis object
        Runnable listenForExpiry = new ExpiryListenerRunnable(redisHost, redisPort);
        Thread thread = threadFactory.newThread(listenForExpiry);
        thread.start();
    }

    public RedisDao() throws StauserverException
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
    public List<String> getRegisteredUsers()
    {
        return jedis.lrange(LIST_USERS, 0, -1);
    }

    @Override
    public long createUser(String id, String hash)
    {
        Transaction transaction = jedis.multi();
        transaction.sadd(USER_HASH_SET, hash);
        transaction.lpush(LIST_USERS, id);
        List<Response<?>> responses = transaction.execGetResponse();

        return (Long) responses.get(0).get() & (Long) responses.get(1).get();
    }

    @Override
    public void deleteUser(String id, String hash) throws NotFoundException
    {
        Transaction transaction = jedis.multi();
        transaction.srem(USER_HASH_SET, hash);
        transaction.lrem(LIST_USERS, 0, id);

        List<Response<?>> responses = transaction.execGetResponse();

        if (((Long) responses.get(0).get() & (Long) responses.get(1).get()) != 1)
        {
            throw new NotFoundException("Could not delete user " + id);
        }
    }

    @Override
    public boolean userIsRegistered(String hash)
    {
        return jedis.sismember(USER_HASH_SET, hash);
    }

    private class ExpiryListenerRunnable implements Runnable
    {
        private String redisHost;
        private int redisPort;

        public ExpiryListenerRunnable(String redisHost, int redisPort)
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


