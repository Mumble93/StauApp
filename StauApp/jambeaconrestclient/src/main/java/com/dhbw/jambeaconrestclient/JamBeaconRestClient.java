package com.dhbw.jambeaconrestclient;

import android.util.Log;

import com.dhbw.jambeaconrestclient.exception.JamBeaconException;

import java.io.IOException;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class JamBeaconRestClient
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private final String SERVER_ADDRESS;

    public JamBeaconRestClient(String endpoint)
    {
        this.SERVER_ADDRESS = endpoint;
    }

    public JamBeaconRestClient()
    {
        this("www.dhbw-jambeacon.org/jams");
    }

    public TrafficJam post(TrafficJam trafficJam) throws JamBeaconException
    {
        return null;
    }

    public TrafficJam get() throws JamBeaconException
    {
        return null;
    }

    public TrafficJam get(UUID id)
    {
        return null;
    }

    public void put(TrafficJam trafficJam) throws JamBeaconException
    {

    }

    public String getEndpoint()
    {
        return this.SERVER_ADDRESS;
    }

    private HttpsURLConnection openConnection(boolean doPost) throws JamBeaconException
    {
        HttpsURLConnection connection;

        try
        {
            URL url = new URL(SERVER_ADDRESS);
            connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(doPost);
            connection.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e)
        {
            Log.e(TAG, "Error trying to open connection to server");
            throw new JamBeaconException(e.getMessage(), e);
        }

        return connection;
    }
}
