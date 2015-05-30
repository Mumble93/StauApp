import android.test.AndroidTestCase;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sta.dhbw.jambeaconrestclient.TrafficJam;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;


public class HttpTest extends AndroidTestCase
{
    public void heartBeatTest() throws IOException
    {
        String endpoint = "http://localhost:8080/rest/api/v1/heartbeat";
        URL url = new URL(endpoint);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        int responseCode = connection.getResponseCode();
        connection.disconnect();

        assertEquals(HttpURLConnection.HTTP_OK, responseCode);
    }

    public void registerUserTest() throws IOException
    {
        String userId = "connectionTest1";

        int userIdSize = userId.length();

        String endpoint = "http://localhost:8080/rest/api/v1/users/register";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "text/plain");
        connection.setRequestProperty("Accept", "text/plain");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setFixedLengthStreamingMode(userIdSize);

        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

        out.write(userId);
        out.flush();
        out.close();

        assertEquals(HttpURLConnection.HTTP_CREATED, connection.getResponseCode());

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = in.readLine()) != null)
        {
            System.out.println(line);
        }

        in.close();
        connection.disconnect();
    }

    public void getJamTest() throws IOException
    {
        String endpoint = "http://localhost:8080/rest/api/v1/jams/5bb3a709-9b95-4bd6-949d-bad2b87c23e1";
        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoInput(true);
        connection.setRequestProperty("Accept", "application/json");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

        assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
        String line = reader.readLine();
        reader.close();

        TrafficJam jam = new ObjectMapper().readValue(line, TrafficJam.class);

        connection.disconnect();
    }
}
