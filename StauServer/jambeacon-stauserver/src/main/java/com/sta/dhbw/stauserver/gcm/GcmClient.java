package com.sta.dhbw.stauserver.gcm;


import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.util.Constants;

import javax.json.*;
import javax.ws.rs.client.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class GcmClient
{
    private static final String API_KEY = "AIzaSyD9zwvulCjrJwofmO3ZpLsBbqOyC8Q12Vc";
    private static final String TARGET = "https://android.googleapis.com/gcm/send";

    private static final String RECIPIENTS = "registration_ids";
    private static final String TTL_FIELD = "time_to_live";
    private static final int TTL_VALUE = 600; //Messages will be stored for 10 minutes

    private static final String PAYLOAD = "data";

    //For testing only
    private static final String TEST_FLAG = "dry_run";
    private static final boolean TEST_FLAG_VAL = true;

    private IBeaconDb dao;

    public GcmClient(IBeaconDb dao)
    {
        this.dao = dao;
    }

    public void sendToGcm()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(TARGET);

        Response response = target.request(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, "key="+API_KEY)
                .post(Entity.json(buildMessage(true)));

        //ToDo: Implement exponential Backoff
    }

    private JsonObject buildMessage(boolean dryRun)
    {
        //send-to-sync message without payload
        return buildMessage(null, dryRun);
    }

    private JsonObject buildMessage(TrafficJamResource jamResource, boolean dryRun)
    {
        JsonObjectBuilder message =  Json.createObjectBuilder()
                .add(TTL_FIELD, TTL_VALUE)
                .add(RECIPIENTS, dao.getRecipientString());
        if(dryRun)
        {
            message.add(TEST_FLAG, TEST_FLAG_VAL);
        }

        if(null != jamResource)
        {
            message.add(PAYLOAD, Json.createObjectBuilder()
                .add(Constants.JAM_ID, jamResource.getJamId().toString())
                .add(Constants.JAM_LATITUDE, jamResource.getLatitude())
                .add(Constants.JAM_LONGITUDE, jamResource.getLongitude())
                .add(Constants.JAM_TIME, jamResource.getTimestamp())
                .build());
        }

        return message.build();

    }
}
