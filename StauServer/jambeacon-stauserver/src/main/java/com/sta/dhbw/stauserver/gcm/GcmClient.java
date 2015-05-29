package com.sta.dhbw.stauserver.gcm;


import com.google.android.gcm.server.*;
import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;


@Singleton
@Startup
@DependsOn("RedisDao")
public class GcmClient
{
    private static final Logger log = LoggerFactory.getLogger(GcmClient.class);

    private static final String API_KEY = "AIzaSyBxYprfcDs3b3x34pDW8HFDaljnTB-rEQ4";
    //private static final String TARGET = "https://android.googleapis.com/gcm/send";

    private static final int TTL_VALUE = 600; //Messages will be stored for 10 minutes
    private static final int MAX_RETRIES = 10; //Beware that exponential backoff is used on retry

    @Resource
    private static ManagedThreadFactory threadFactory;

    @EJB
    private static IBeaconDb dao;

    public GcmClient() throws StauserverException
    {
        try
        {
            if (null == threadFactory)
            {
                synchronized (GcmClient.class)
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
    }


    public void sendToGcm(Message message)
    {
        Runnable messageRunnable = new MessageRunnable(message, dao.getRegisteredUsers());
        Thread thread = threadFactory.newThread(messageRunnable);
        thread.start();
    }

    public Message buildMessage(TrafficJamResource jamResource)
    {
        Message.Builder builder = new Message.Builder()
                .timeToLive(TTL_VALUE);
        if (null == jamResource)
        {
            return builder
                    .collapseKey("syncTrafficJams")
                    .build();
        } else
        {
            return builder
                    .addData(Constants.JAM_ID, jamResource.getJamId().toString())
                    .addData(Constants.JAM_TIME, String.valueOf(jamResource.getTimestamp()))
                    .addData(Constants.JAM_LATITUDE, String.valueOf(jamResource.getLatitude()))
                    .addData(Constants.JAM_LONGITUDE, String.valueOf(jamResource.getLongitude()))
                    .build();
        }
    }

    private class MessageRunnable implements Runnable
    {
        private final Logger log = LoggerFactory.getLogger(MessageRunnable.class);

        public Message message;
        public List<String> recipients;

        public MessageRunnable(Message message, List<String> recipients)
        {
            this.message = message;
            this.recipients = recipients;
        }

        private void handleMulticastResult(MulticastResult multicastResult)
        {
            if (null == multicastResult)
            {
                log.error("Error, result was null.");
            } else if (multicastResult.getFailure() == 0 && multicastResult.getCanonicalIds() == 0)
            {
                //Request was processed successfully
                //Remainder of response does not have to be parsed
                log.info("Message sent successfully, MulticastId: " + multicastResult.getMulticastId());
            } else
            {
                List<Result> messageResultList = multicastResult.getResults();
                for (Result messageResult : messageResultList)
                {
                    if (messageResult.getMessageId() == null)
                    {
                        String errorCode = messageResult.getErrorCodeName();
                        if (com.google.android.gcm.server.Constants.ERROR_NOT_REGISTERED.equals(errorCode))
                        {
                            int resultMessageIndex = messageResultList.indexOf(messageResult);
                            String userId = recipients.get(resultMessageIndex);
                            try
                            {
                                dao.deleteUser(userId, null);
                            } catch (StauserverException e)
                            {
                                log.error(e.getMessage());
                            }
                        } else
                        {
                            log.error("Sending message to GCM failed because of " + errorCode);
                        }
                    } else
                    {
                        if (messageResult.getCanonicalRegistrationId() != null)
                        {
                            int resultMessageIndex = messageResultList.indexOf(messageResult);
                            String originalId = recipients.get(resultMessageIndex);
                            try
                            {
                                dao.updateUser(originalId, messageResult.getCanonicalRegistrationId());
                            } catch (StauserverException e)
                            {
                                log.error(e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void run()
        {
            Sender sender = new Sender(API_KEY);
            MulticastResult result = null;
            try
            {
                result = sender.send(message, recipients, MAX_RETRIES);
            } catch (InvalidRequestException | IllegalArgumentException e)
            {
                log.error("Message was not accepted. " + e.getMessage());
            } catch (IOException e)
            {
                log.error("Message could not be sent. " + e.getMessage());
            }

            handleMulticastResult(result);
            log.info("Exiting MessageRunnable Thread now...");
        }
    }
}
