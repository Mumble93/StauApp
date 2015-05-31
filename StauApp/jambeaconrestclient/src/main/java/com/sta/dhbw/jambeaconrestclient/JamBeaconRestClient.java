package com.sta.dhbw.jambeaconrestclient;

import android.os.AsyncTask;
import android.util.Log;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import static com.sta.dhbw.jambeaconrestclient.util.Constants.APPLICATION_JSON;
import static com.sta.dhbw.jambeaconrestclient.util.Constants.HTTP_DELETE;
import static com.sta.dhbw.jambeaconrestclient.util.Constants.HTTP_GET;
import static com.sta.dhbw.jambeaconrestclient.util.Constants.HTTP_POST;
import static com.sta.dhbw.jambeaconrestclient.util.Constants.HTTP_PUT;
import static com.sta.dhbw.jambeaconrestclient.util.Constants.TEXT_PLAIN;


public class JamBeaconRestClient implements Serializable
{
    private static final String TAG = JamBeaconRestClient.class.getSimpleName();

    private static String SERVER_ENDPOINT, REGISTER_ENDPOINT, UNREGISTER_ENDPOINT, UPDATE_ENDPOINT, JAM_ENDPOINT, HEARTBEAT_ENDPOINT;

    private static final String X_REQUEST_HEADER = "X-Request-Id";

    public JamBeaconRestClient()
    {
        Log.i(TAG, "Starting in DEBUG Mode.");
        SERVER_ENDPOINT = "http://localhost:8080/rest/api/v1/";
        //SERVER_ENDPOINT = "http://www.dhbw-jambeacon.org/rest/api/v1/";
        REGISTER_ENDPOINT = SERVER_ENDPOINT + "users/register";
        UNREGISTER_ENDPOINT = SERVER_ENDPOINT + "users/unregister/";
        UPDATE_ENDPOINT = SERVER_ENDPOINT + "users/update";
        JAM_ENDPOINT = SERVER_ENDPOINT + "jams";
        HEARTBEAT_ENDPOINT = SERVER_ENDPOINT + "heartbeat";
    }


    public void registerUser(String userId, final ICallBackInterface caller) throws JamBeaconException
    {
        Log.d(TAG, "REGISTERING USER");
        new AsyncTask<String, Void, String>()
        {
            HttpURLConnection connection;

            @Override
            protected String doInBackground(String... params)
            {
                String userId = params[0];
                try
                {
                    connection = getConnection(HTTP_POST, REGISTER_ENDPOINT);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write(userId);
                    writer.flush();
                    writer.close();

                    int statusCode = connection.getResponseCode();
                    switch (statusCode)
                    {
                        case HttpURLConnection.HTTP_CREATED:
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String xRequestId = reader.readLine();
                            Log.d(TAG, "Response at REGISTER was " + xRequestId);
                            reader.close();
                            return xRequestId;
                        case HttpURLConnection.HTTP_CONFLICT:
                            Log.e(TAG, "Already registered with Id " + userId);
                            return "";
                        default:
                            Log.e(TAG, "Could not register, response code was " + statusCode);
                            return "";
                    }
                } catch (JamBeaconException e)
                {
                    Log.e(TAG, e.getMessage());
                    return "";
                } catch (IOException e)
                {
                    Log.e(TAG, e.getMessage());
                    return "";
                }
            }

            @Override
            protected void onPostExecute(String result)
            {
                connection.disconnect();
                caller.onRegisterComplete(result);
            }
        }.execute(userId);

    }

    public void updateUser(String oldId, String updatedId, final String xRequestHeader, final ICallBackInterface caller) throws JamBeaconException
    {
        Log.d(TAG, "UPDATING USER");
        new AsyncTask<String, Void, String>()
        {
            HttpURLConnection connection;

            @Override
            protected String doInBackground(String... params)
            {
                try
                {
                    connection = getConnection(HTTP_PUT, UPDATE_ENDPOINT);
                    connection.setDoInput(true);
                    connection.setDoOutput(true);
                    connection.setRequestProperty(X_REQUEST_HEADER, xRequestHeader);

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
                    writer.write(params[0]);
                    writer.flush();
                    writer.close();

                    int statusCode = connection.getResponseCode();

                    if(statusCode==HttpURLConnection.HTTP_OK|| statusCode== HttpURLConnection.HTTP_CREATED)
                    {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        String updatedXRequestId = reader.readLine();
                        reader.close();
                        return updatedXRequestId;
                    } else
                    {
                        Log.e(TAG, "Error updating User Id, status was " + statusCode);
                        return "";
                    }
                } catch (JamBeaconException e)
                {
                    Log.e(TAG, "Error getting connection. " + e.getMessage());
                    return "";
                } catch (IOException e)
                {
                    Log.e(TAG, "Error while sending to server. " + e.getMessage());
                    return "";
                }
            }

            @Override
            protected void onPostExecute(String result)
            {
                connection.disconnect();
                caller.onUserUpdateComplete(result);
            }
        }.execute(oldId + ";" + updatedId);

    }

    public void unregisterUser(String userId) throws JamBeaconException
    {

    }


    public TrafficJam postTrafficJam(TrafficJam trafficJam, String xRequestId) throws JamBeaconException
    {
        return null;
    }

    public List<TrafficJam> getTrafficJamList() throws JamBeaconException
    {
        return null;
    }

    public TrafficJam getTrafficJam(UUID id) throws JamBeaconException
    {
        return null;
    }

    public void updateTrafficJam(TrafficJam trafficJam, String xRequestId) throws JamBeaconException
    {

    }

    public static class AvailabilityTask extends AsyncTask<Void, Void, Boolean>
    {
        private ICallBackInterface callback;

        public AvailabilityTask(ICallBackInterface callback)
        {
            this.callback = callback;
        }

        @Override
        protected Boolean doInBackground(Void... params)
        {
            int responseCode = 418;//I'm a teapot

            try
            {
                HttpURLConnection connection = getConnection(HTTP_GET, HEARTBEAT_ENDPOINT);
                responseCode = connection.getResponseCode();
                connection.disconnect();
            } catch (JamBeaconException e)
            {
                String error = "Error getting connection. " + e.getMessage();
                Log.e(TAG, error);
            } catch (IOException e)
            {
                String error = "ERROR getting HeartBeat Response Code. " + e.getMessage();
                Log.e(TAG, error);
            }
            return HttpURLConnection.HTTP_OK == responseCode;
        }

        @Override
        protected void onPostExecute(Boolean result)
        {
            callback.onCheckComplete(result);
        }
    }


    public String getEndpoint()
    {
        return SERVER_ENDPOINT;
    }

    private static HttpURLConnection getConnection(String method, String endpoint) throws JamBeaconException
    {
        HttpURLConnection connection;

        try
        {
            URL url = new URL(endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            String acceptHeader = getAcceptHeader(method, endpoint);
            String contentType = getContentType(method, endpoint);
            if (!HEARTBEAT_ENDPOINT.equals(endpoint))
            {
                connection.setRequestProperty("Accept", acceptHeader);
                connection.setRequestProperty("Content-Type", contentType);
            }
        } catch (MalformedURLException e)
        {
            String error = "ERROR getting endpoint url. Endpoint was " + endpoint + " " + e.getMessage();
            Log.e(TAG, error);
            throw new JamBeaconException(error, e);
        } catch (IOException e)
        {
            String error = "ERROR opening connection. " + e.getMessage();
            Log.e(TAG, error);
            throw new JamBeaconException(error, e);
        }

        return connection;
    }

    private static String getAcceptHeader(String method, String endpoint)
    {
        String acceptHeader = "";

        if (REGISTER_ENDPOINT.equals(endpoint) || UPDATE_ENDPOINT.equals(endpoint))
        {
            acceptHeader = TEXT_PLAIN;
        } else if (JAM_ENDPOINT.equals(endpoint) && !HTTP_DELETE.equals(method))
        {
            acceptHeader = APPLICATION_JSON;
        }

        return acceptHeader;
    }

    private static String getContentType(String method, String endpoint)
    {
        String contentType = "";
        if (REGISTER_ENDPOINT.equals(endpoint) || UPDATE_ENDPOINT.equals(endpoint))
        {
            contentType = TEXT_PLAIN;
        } else if (JAM_ENDPOINT.equals(endpoint) && HTTP_PUT.equals(method) || HTTP_POST.equals(method))
        {
            contentType = APPLICATION_JSON;
        }

        return contentType;
    }
}
