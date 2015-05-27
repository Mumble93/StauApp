package com.sta.dhbw.jambeaconrestclient;

import android.util.Log;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;
import com.sta.dhbw.jambeaconrestclient.model.TrafficJamDTO;
import com.sta.dhbw.jambeaconrestclient.model.UserDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class JamBeaconRestClient
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private String SERVER_ENDPOINT = "http://10.0.2.2:8080/jambeacon-stauserver-1.0-SNAPSHOT/api/v1/jams";

    public JamBeaconRestClient(String endpoint)
    {
        this.SERVER_ENDPOINT = endpoint;
    }

    public JamBeaconRestClient()
    {
        //this("http://www.dhbw-jambeacon.org/jambeacon-stauserver-1.0-SNAPSHOT/api/v1/jams");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void registerUser(UserDTO user)
    {
        Client client = ClientBuilder.newClient();
        WebTarget target = client.target(SERVER_ENDPOINT);
        Response response = target.request().post(Entity.json(user));
        if (response.getStatusInfo() != Response.Status.CREATED)
        {
            Log.e(TAG, "Error registering at server.");
        }
    }


    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public TrafficJam post(TrafficJam trafficJam) throws JamBeaconException
    {
        TrafficJamDTO transportObject = TrafficJamSerializer.trafficJamToDto(trafficJam);

        Client client = ClientBuilder.newClient();
        Response response = client.target(SERVER_ENDPOINT)
                .request().post(Entity.entity(transportObject, MediaType.APPLICATION_JSON));
        if (response.getStatusInfo() != Response.Status.CREATED)
        {
            String error = "Error posting Traffic Jam to server.";
            Log.e(TAG, error);
            throw new JamBeaconException(error);
        }else
        {
            TrafficJamDTO responseEntity = (TrafficJamDTO) response.getEntity();
            return TrafficJamDeserializer.dtoToTrafficJam(responseEntity);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TrafficJamDTO> get() throws JamBeaconException
    {
        List<TrafficJamDTO> result = new ArrayList<>();

        Client client = ClientBuilder.newClient();
        Response response = client.target(SERVER_ENDPOINT)
                .request().get();

        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public TrafficJam get(UUID id) throws JamBeaconException
    {
        return null;
    }

    public void put(TrafficJam trafficJam) throws JamBeaconException
    {

    }

    public String getEndpoint()
    {
        return this.SERVER_ENDPOINT;
    }

}
