package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.util.Constants;
import com.sta.dhbw.stauserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

import javax.annotation.PreDestroy;
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

/**
 * A database access object that wraps the functionality of the Jedis Client to communicate with a REDIS instance. <br>
 * An Enterprise Singleton Bean, will be initialized at Server startup.
 */
@Singleton
@Startup
public class RedisDao implements IBeaconDb
{
    private static final Logger log = LoggerFactory.getLogger(RedisDao.class);

    private Jedis jedis;

    @Resource
    private static ManagedThreadFactory threadFactory;
    private static Thread listenerThread;

    private static final String REDIS_RESPONSE_OK = "OK";
    private static final String REDIS_KEYEVENT_CHANNEL = "__keyevent@0__:expired";

    private static final String FIELD_JAM = "jam:";
    private static final String LIST_JAM = "jams";
    private static final String LIST_USERS = "users";
    private static final String USER_HASH_SET = "users:hashes";

    /**
     * This constructor will instantiate two Jedis Client objects, one for database interaction and one
     * for listening on the keyevent channel, on the given host and port. The listening instance will start on a
     * new Thread, instantiated by the server's default ManagedThreadFactory, since the subscribe operation is synchronous.
     *
     * @param redisHost The host of the REDIS instance.
     * @param redisPort The port of the REDIS instance
     * @throws StauserverException
     */
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
        listenerThread = threadFactory.newThread(listenForExpiry);

        listenerThread.start();
    }

    /**
     * The default constructor will connect to the standard REDIS port, 6379, on localhost
     *
     * @throws StauserverException
     */
    public RedisDao() throws StauserverException
    {
        this("localhost", 6379);
    }

    /**
     * At server shutdown, this method checks whether the Thread listening on the keyevent channel is still running.
     * If so, shutdown will be suspended until this Thread is interrupted.<br>
     * This is to prevent the existence of multiple Threads listening on the same channel, which prevents thinning
     * out the ThreadPool and overflowing the server log with redundant messages.
     */
    @PreDestroy
    public void tearDown()
    {
        if (listenerThread.isAlive())
        {
            log.info("Listener Thread still alive. Shutting down now.");
            listenerThread.interrupt();
            while (listenerThread.isAlive())
            {
                log.info("Waiting for Listener Thread to shut down...");
                if (listenerThread.isInterrupted() || !listenerThread.isAlive())
                {
                    log.info("Listener Thread shut down.");
                    break;
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAlive()
    {
        return jedis.isConnected();
    }

    /**
     * {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TrafficJamResource> getTrafficJamList()
    {
        ArrayList<TrafficJamResource> resultList = new ArrayList<>();

        List<String> jamlist = jedis.lrange(LIST_JAM, 0, -1);
        if (jamlist != null && !jamlist.isEmpty())
        {
            for (String id : jamlist)
            {
                Map<String, String> attributeMap = jedis.hgetAll(FIELD_JAM + id);
                if (attributeMap != null && !attributeMap.isEmpty())
                {
                    resultList.add(Util.trafficJamFromMap(attributeMap));
                }
            }
        }
        return resultList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void storeTrafficJam(TrafficJamResource jam) throws StauserverException
    {
        String jamId = jam.getJamId().toString();

        Transaction transaction = jedis.multi();
        transaction.hmset(FIELD_JAM + jamId, Util.trafficJamToMap(jam));
        transaction.lpush(LIST_JAM, jamId);
        //Map the jam to the owner, for update purposes
        transaction.set(jamId, jam.getOwner());
        //The jam will be deleted after 10 minutes (600 seconds)
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
                //LPUSH operation responds with length of list after push
                //EXPIRE will return 1 on success
                if ((Long) response.get() < 0L)
                {
                    String error = "Error storing new Traffic Jam with params: \n" + jam.toJsonObject().toString();
                    throw new StauserverException(error);
                }
            }
        }

        log.info("Created new Traffic Jam with id: " + jamId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrafficJam(TrafficJamResource trafficJam)
    {
        updateTrafficJam(trafficJam, false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTrafficJam(TrafficJamResource trafficJam, boolean updateOwner)
    {
        String jamId = FIELD_JAM + trafficJam.getJamId().toString();

        Map<String, String> updatedJamValues = Util.trafficJamToMap(trafficJam);
        Map<String, String> existingJamValues = jedis.hgetAll(jamId);

        //Id should not be overwritten
        existingJamValues.put(Constants.JAM_LATITUDE, updatedJamValues.get(Constants.JAM_LATITUDE));
        existingJamValues.put(Constants.JAM_LONGITUDE, updatedJamValues.get(Constants.JAM_LONGITUDE));
        existingJamValues.put(Constants.JAM_TIME, updatedJamValues.get(Constants.JAM_TIME));

        Transaction transaction = jedis.multi();

        if (updateOwner)
        {
            String updatedOwner = updatedJamValues.get(Constants.JAM_OWNER);
            existingJamValues.put(Constants.JAM_OWNER, updatedOwner);
            transaction.set(trafficJam.getJamId().toString(), updatedOwner);
        }
        transaction.hmset(jamId, existingJamValues);
        //Renew the expiration of the jam to again 10 minutes (600 seconds)
        transaction.expire(jamId, 600);

        transaction.exec();

        log.info("Updated Traffic Jam with Id: " + jamId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTrafficJam(String id) throws NotFoundException
    {
        Transaction transaction = jedis.multi();

        //Number of keys removed should be 1 in both operations
        transaction.del(FIELD_JAM + id);
        transaction.lrem(LIST_JAM, 0, id);
        transaction.del(id);
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

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getRegisteredUsers()
    {
        //LRANGE key start end
        return jedis.lrange(LIST_USERS, 0, -1); //gives back the whole list
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String createUser(String id, String hash) throws StauserverException
    {
        if (null == id || id.isEmpty())
        {
            throw new StauserverException("Error creating User. ID must be set.");
        }

        if (null == hash || hash.isEmpty())
        {
            hash = Util.hash256(id);
        }

        if (jedis.sismember(USER_HASH_SET, hash))
        {
            return "";
        } else
        {
            Transaction transaction = jedis.multi();
            transaction.llen(LIST_USERS);
            transaction.sadd(USER_HASH_SET, hash);
            transaction.lpush(LIST_USERS, id);
            transaction.exec();

            log.info("Created User " + id);

            return hash;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long deleteUser(String id, String hash) throws StauserverException
    {
        if (null == id || id.isEmpty())
        {
            log.error("DELETE Error: Id was not set.");
            return -1;
        }

        if (null == hash || hash.isEmpty())
        {
            hash = Util.hash256(id);
        }

        Transaction transaction = jedis.multi();
        transaction.srem(USER_HASH_SET, hash);
        transaction.lrem(LIST_USERS, 0, id);

        List<Response<?>> responses = transaction.execGetResponse();

        //In both cases the returned value should be 1, since 1 element should have been removed respectively
        return (Long) responses.get(0).get() & (Long) responses.get(1).get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateUser(String oldId, String updatedId) throws StauserverException
    {
        String oldIdHash = Util.hash256(oldId);
        String updatedIdHash = Util.hash256(updatedId);

        Transaction transaction = jedis.multi();


        List<String> jamList = jedis.lrange(LIST_JAM, 0, -1);
        for (String jamId : jamList)
        {
            if (jedis.exists(jamId) && oldId.equals(jedis.get(jamId)))
            {
                transaction.set(jamId, updatedId);
                break;
            }
        }

        //Remove old Id, add new one
        transaction.lrem(LIST_USERS, 0, oldId);
        transaction.lpush(LIST_USERS, updatedId);
        //Remove old hash, add new one to set
        transaction.srem(USER_HASH_SET, oldIdHash);
        transaction.sadd(USER_HASH_SET, updatedIdHash);

        transaction.exec();

        log.info("Updated User " + oldId + " to " + updatedId);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean userIsRegistered(String hash)
    {
        return jedis.sismember(USER_HASH_SET, hash);
    }

    /**
     * At instantiation of the RedisDao class, this will create a new Jedis object which will be used to subscribe to the
     * keyevent channel and listen for "expired" events
     */
    private class ExpiryListenerRunnable implements Runnable
    {
        private String redisHost;
        private int redisPort;

        /**
         * Host and Port should be the same as the database used for storing jams
         *
         * @param redisHost The host, as String
         * @param redisPort The port, as int
         */
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

    /**
     * This class is used to react to "expired" events that are sent on the keyevent channel.<br>
     * If such an event is fired, the Id of the expired resource will be removed from the separate list, holding all
     * Ids of all stored jams.
     */
    private class ExpiredListener extends JedisPubSub
    {
        private final Logger log = LoggerFactory.getLogger(ExpiredListener.class);

        public void onMessage(String channel, String message)
        {
            if (REDIS_KEYEVENT_CHANNEL.equals(channel))
            {
                String[] messageAttributes = message.split(":");
                String trafficJamId = messageAttributes[1];

                Transaction transaction = jedis.multi();
                transaction.lrem(LIST_JAM, 0, trafficJamId);
                transaction.del(trafficJamId);
                transaction.exec();
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


