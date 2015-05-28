package com.sta.dhbw.jambeaconrestclient;

import android.content.SharedPreferences;
import android.util.Log;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.jambeaconrestclient.model.TrafficJamDTO;

import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JamBeaconRestClient
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private static boolean isInDebugMode;

    private String SERVER_ENDPOINT;
    private String REGISTER_ENDPOINT = SERVER_ENDPOINT + "users/register";
    private String UNREGISTER_ENDPOINT = SERVER_ENDPOINT + "users/unregister/{userId}";
    private String JAM_ENDPOINT = SERVER_ENDPOINT + "jams";

    private static String X_REQUEST_ID;
    private static final String X_REQUEST_HEADER = "X-Request-Id";

    public JamBeaconRestClient(SharedPreferences sharedPreferences)
    {
        isInDebugMode = android.os.Debug.isDebuggerConnected();

        X_REQUEST_ID = sharedPreferences.getString("registration_id", "");
        Log.i(TAG, "Initialized with registration id " + X_REQUEST_ID);

        if(isInDebugMode)
        {
            Log.i(TAG, "Starting in DEBUG Mode.");
            this.SERVER_ENDPOINT = "http://localhost:8080/api/v1/";
        } else
        {
            this.SERVER_ENDPOINT = "http://www.dhbw-jambeacon.org/api/v1/";
        }
    }

    @POST
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.TEXT_PLAIN)
    public String registerUser(String userId) throws JamBeaconException
    {
        if(isInDebugMode)
        {
            userId = "appTestUser42";
        }
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(REGISTER_ENDPOINT);
        Response response = target.request().post(Entity.text(userId));
        if (response.getStatusInfo() == Response.Status.CONFLICT)
        {
            String error = "Already registered.";
            Log.e(TAG, error);
            return "";
        } else if (response.getStatusInfo() != Response.Status.CREATED)
        {
            String error = "ERROR while registering at server.";
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }else
        {
            return  response.readEntity(String.class);
        }
    }

    @DELETE
    public void unregisterUser(String userId) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(UNREGISTER_ENDPOINT)
                .resolveTemplate("userId", userId);
        Response response = target.request().delete();
        Response.StatusType statusType = response.getStatusInfo();
        response.close();
        if (statusType != Response.Status.OK || statusType != Response.Status.NOT_FOUND)
        {
            String error = "ERROR while unregistering. Status was" + statusType.getStatusCode();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public TrafficJam postTrafficJam(TrafficJam trafficJam) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);

        Response response = target.request()
                .header(X_REQUEST_HEADER, X_REQUEST_ID)
                .post(Entity.json(trafficJam));

        response.close();
        if (response.getStatusInfo() != Response.Status.CREATED)
        {
            String error = "Error posting Traffic Jam to server.";
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }else
        {
            return response.readEntity(TrafficJam.class);
        }
    }

    @GET
    public List<TrafficJamDTO> getTrafficJamList() throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);
        Response response = target.request()
                .get();

        if(response.getStatusInfo() == Response.Status.OK)
        {
            return response.readEntity(new GenericType<List<TrafficJamDTO>>(){});
        } else if (response.getStatusInfo() == Response.Status.NO_CONTENT)
        {
            return null;
        } else
        {
            String error = "ERROR getting List of Traffic Jams. Status was " +response.getStatus();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }
    }

    @GET
    public TrafficJam getTrafficJam(UUID id) throws JamBeaconException
    {
        String jamId = id.toString();
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT + "/{id}");
        Response response = target.resolveTemplate("id", jamId)
                .request()
                .get();
        if(response.getStatusInfo() == Response.Status.OK)
        {
            return response.readEntity(TrafficJam.class);
        } else if (response.getStatusInfo() == Response.Status.NOT_FOUND)
        {
            return null;
        } else
        {
            String error = "ERROR while getting Jam " + jamId + ". Status was " + response.getStatus();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateTrafficJam(TrafficJam trafficJam) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);
        Response response = target.request()
                .header(X_REQUEST_HEADER, X_REQUEST_ID)
                .put(Entity.json(trafficJam));

        Response.StatusType statusType = response.getStatusInfo();

        if (statusType != Response.Status.OK || statusType != Response.Status.CREATED)
        {
            String error = "ERROR updating traffic jam. Status was " + statusType.getStatusCode();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }
    }

    public String getEndpoint()
    {
        return this.SERVER_ENDPOINT;
    }

}
