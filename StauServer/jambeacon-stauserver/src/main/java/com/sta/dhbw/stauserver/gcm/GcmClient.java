package com.sta.dhbw.stauserver.gcm;


import com.google.android.gcm.server.*;
import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.util.List;

/**
 * This class wraps the communication with Google Cloud Messaging service, using the gcm-server helper library.
 */
public class GcmClient
{
    private static final Logger log = LoggerFactory.getLogger(GcmClient.class);

    private static final String API_KEY = "AIzaSyD9zwvulCjrJwofmO3ZpLsBbqOyC8Q12Vc";
    //private static final String TARGET = "https://android.googleapis.com/gcm/send";

    private static final int TTL_VALUE = 600; //Messages will be stored for 10 minutes
    private static final int MAX_RETRIES = 10; //Beware that exponential backoff is used on retry

    @Resource
    private static ManagedThreadFactory threadFactory;

    private IBeaconDb dao;

    /**
     * Constructor gets the default ManagedThreadFactory of the server. Separate Threads need to used to send messages,
     * since in case of unavailability, exponential backoff is used to retry sending.
     *
     * @param dao The DAO that is connected to the database which holds users and jams.
     */
    public GcmClient(IBeaconDb dao)
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
        }

        this.dao = dao;
    }


    /**
     * Creates a new Thread with a MessageRunnable that tries sending the Message.
     *
     * @param message The Message to be sent.
     */
    public void sendToGcm(Message message)
    {
        Runnable messageRunnable = new MessageRunnable(message, dao.getRegisteredUsers());
        Thread thread = threadFactory.newThread(messageRunnable);
        thread.start();
    }

    /**
     * Builds a message that tells the devices to delete a certain jam from the local list.
     *
     * @param jamId The Id of the traffic jam object to be deleted, as String
     * @return The Message object to be sent
     */
    public Message buildDeleteMessage(String jamId)
    {
        return new Message.Builder()
                .timeToLive(TTL_VALUE)
                .addData("type", "delete")
                .addData(Constants.JAM_ID, jamId)
                .delayWhileIdle(false)
                .build();
    }

    /**
     * Builds a Message that will deliver the details of a freshly reported traffic jam.<br>
     * If parameter is null, a Send-To-Sync Message will be returned.
     *
     * @param jamResource The freshly posted TrafficJamResource
     * @return A Message containing the information of the TrafficJamResource
     */
    public Message buildJamMessage(TrafficJamResource jamResource)
    {
        Message.Builder builder = new Message.Builder()
                .timeToLive(TTL_VALUE);
        if (null == jamResource)
        {
            return builder
                    .collapseKey("syncTrafficJams")
                    .delayWhileIdle(false)
                    .build();
        } else
        {
            return builder
                    .addData("type", "jam")
                    .addData(Constants.JAM_ID, jamResource.getJamId().toString())
                    .addData(Constants.JAM_TIME, String.valueOf(jamResource.getTimestamp()))
                    .addData(Constants.JAM_LATITUDE, String.valueOf(jamResource.getLatitude()))
                    .addData(Constants.JAM_LONGITUDE, String.valueOf(jamResource.getLongitude()))
                    .delayWhileIdle(false)
                    .build();
        }
    }

    /**
     * This class is used to send Messages on a separate Thread, since the send operation will block the calling Thread
     * until the Message is sent. In case of GCM unavailability, exponential backoff is used, which can take up several
     * minutes.
     */
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

        /**
         * Sifts through the result of a sending operation to see if action must be taken.
         *
         * @param multicastResult The MulticastResult to analyse
         */
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
                            //User has unregistered from GCM or deleted app. Delete the Id from the database
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
                            //Message has been delivered, but the user is using a different Id. Replace the existing
                            //one with the new Id.
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

        /**
         * Sends the Message to the GCM Service.
         */
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
