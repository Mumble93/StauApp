package com.sta.dhbw.jambeaconrestclient;

import android.util.Log;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;

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

@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN})
@Produces({MediaType.TEXT_PLAIN})
public class JamBeaconRestClient
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private static String SERVER_ENDPOINT;
    private String REGISTER_ENDPOINT = SERVER_ENDPOINT + "users/register";
    private String UNREGISTER_ENDPOINT = SERVER_ENDPOINT + "users/unregister/{userId}";
    private String UPDATE_ENDPOINT = SERVER_ENDPOINT + "/users/update";
    private String JAM_ENDPOINT = SERVER_ENDPOINT + "jams";

    private static final String X_REQUEST_HEADER = "X-Request-Id";

    public JamBeaconRestClient()
    {
        Log.i(TAG, "Starting in DEBUG Mode.");
        SERVER_ENDPOINT = "http://localhost:8080/api/v1/";
        //SERVER_ENDPOINT = "http://www.dhbw-jambeacon.org/api/v1/";

    }


    @POST
    public String registerUser(String userId) throws JamBeaconException
    {
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
        } else
        {
            return response.readEntity(String.class);
        }
    }

    @PUT
    public String updateUser(String oldId, String updatedId, String xRequestHeader) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(UPDATE_ENDPOINT);
        Response response = target.request()
                .header(X_REQUEST_HEADER, xRequestHeader)
                .put(Entity.text(oldId + ";" + updatedId));
        Response.StatusType statusType = response.getStatusInfo();
        if (statusType != Response.Status.OK || statusType != Response.Status.CREATED)
        {
            String error = "ERROR while updating User Registration at Server. Status was " + statusType.getStatusCode();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        } else
        {
            return response.readEntity(String.class);
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
    public TrafficJam postTrafficJam(TrafficJam trafficJam, String xRequestId) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);

        Response response = target.request()
                .header(X_REQUEST_HEADER, xRequestId)
                .post(Entity.json(trafficJam));

        response.close();
        if (response.getStatusInfo() != Response.Status.CREATED)
        {
            String error = "Error posting Traffic Jam to server.";
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        } else
        {
            return response.readEntity(TrafficJam.class);
        }
    }

    @GET
    public List<TrafficJam> getTrafficJamList() throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);
        Response response = target.request()
                .get();

        if (response.getStatusInfo() == Response.Status.OK)
        {
            return response.readEntity(new GenericType<List<TrafficJam>>()
            {
            });
        } else if (response.getStatusInfo() == Response.Status.NO_CONTENT)
        {
            return null;
        } else
        {
            String error = "ERROR getting List of Traffic Jams. Status was " + response.getStatus();
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
        if (response.getStatusInfo() == Response.Status.OK)
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
    public void updateTrafficJam(TrafficJam trafficJam, String xRequestId) throws JamBeaconException
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(JAM_ENDPOINT);
        Response response = target.request()
                .header(X_REQUEST_HEADER, xRequestId)
                .put(Entity.json(trafficJam));

        Response.StatusType statusType = response.getStatusInfo();

        if (statusType != Response.Status.OK || statusType != Response.Status.CREATED)
        {
            String error = "ERROR updating traffic jam. Status was " + statusType.getStatusCode();
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }
    }

    @GET
    public static boolean serverIsAvailable()
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(SERVER_ENDPOINT + "/heartbeat");
        Response response = target.request().get();
        Response.StatusType statusType = response.getStatusInfo();
        response.close();
        return statusType == Response.Status.OK;
    }

    public String getEndpoint()
    {
        return SERVER_ENDPOINT;
    }

}
