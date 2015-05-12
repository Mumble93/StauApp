package com.dhbw.jambeaconrestclient;

import android.util.Log;

import com.dhbw.jambeaconrestclient.exception.JamBeaconException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;

import javax.net.ssl.HttpsURLConnection;

public class JamBeaconRestClient
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private String SERVER_ENDPOINT;

    public JamBeaconRestClient(String endpoint)
    {
        this.SERVER_ENDPOINT = endpoint;
    }

    public JamBeaconRestClient()
    {
        this("www.dhbw-jambeacon.org/jams");
    }

    public TrafficJam post(TrafficJam trafficJam) throws JamBeaconException
    {
        HttpsURLConnection connection = openConnection("POST");
        OutputStreamWriter out;
        BufferedReader reader;

        try
        {
            out = new OutputStreamWriter(connection.getOutputStream());
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            out.write(TrafficJamSerializer.trafficJamToJson(trafficJam));
            out.flush();
            out.close();

            if (connection.getResponseCode() == HttpURLConnection.HTTP_CREATED)
            {
                //Server should deliver Json object in one line
                String responseString = reader.readLine();
                reader.close();
                return TrafficJamDeserializer.jsonToTrafficJam(responseString);
            } else
            {
                String error = "Error posting resource, code was " + connection.getResponseCode();
                Log.e(TAG, error);
                throw new JamBeaconException(error);
            }
        } catch (IOException e)
        {
            String error = "Error posting jam. ";
            Log.e(TAG, error);
            throw new JamBeaconException(error + e.getMessage(), e);
        } finally
        {
            connection.disconnect();
        }
    }

    public TrafficJam get() throws JamBeaconException
    {
        return get(null);
    }

    public TrafficJam get(UUID id) throws JamBeaconException
    {
        String endpoint = SERVER_ENDPOINT;

        BufferedReader reader;

        if (null != id)
        {
            endpoint += "/" + id.toString();
        }
        HttpsURLConnection connection = openConnection(endpoint, "GET");

        try
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                //Server should deliver response as one line
                String response = reader.readLine();
                reader.close();

                return TrafficJamDeserializer.jsonToTrafficJam(response);
            } else
            {
                String error = "Error getting resource, code was " + connection.getResponseCode();
                Log.e(TAG, error);
                throw new JamBeaconException(error);
            }
        } catch (IOException e)
        {
            String error = "Error getting resource. ";
            Log.e(TAG, error);
            throw new JamBeaconException(error + e.getMessage(), e);
        } finally
        {
            connection.disconnect();
        }
    }

    public void put(TrafficJam trafficJam) throws JamBeaconException
    {
        String id = trafficJam.getId().toString();
        String endpoint = SERVER_ENDPOINT + "/" + id;
        HttpsURLConnection connection = openConnection(endpoint, "PUT");

        OutputStreamWriter writer;

        try
        {
            writer = new OutputStreamWriter(connection.getOutputStream());

            writer.write(TrafficJamSerializer.trafficJamToJson(trafficJam));
            writer.flush();
            writer.close();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
            {
                String error = "Error updating resource, code was " + connection.getResponseCode();
                Log.e(TAG, error);
                throw new JamBeaconException(error);
            }

        } catch (IOException e)
        {
            String error = "Error updating jam. ";
            Log.e(TAG, error);
            throw new JamBeaconException(error + e.getMessage(), e);
        } finally
        {
            connection.disconnect();
        }
    }

    public String getEndpoint()
    {
        return this.SERVER_ENDPOINT;
    }

    private HttpsURLConnection openConnection(String method) throws JamBeaconException
    {
        return openConnection(SERVER_ENDPOINT, method);
    }

    private HttpsURLConnection openConnection(String url, String method) throws JamBeaconException
    {
        HttpsURLConnection connection;

        try
        {
            URL endpoint = new URL(url);
            connection = (HttpsURLConnection) endpoint.openConnection();
            if (!("GET").equals(method))
            {
                connection.setDoOutput(true);
                connection.setRequestMethod(method);
                connection.setRequestProperty("Content-Type", "application/json");
            }
            connection.setRequestProperty("Accept", "application/json");
        } catch (IOException e)
        {
            String msg = "Error trying to open connection to server. ";
            Log.e(TAG, msg);
            throw new JamBeaconException(msg + e.getMessage(), e);
        }

        return connection;
    }
}
