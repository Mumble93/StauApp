import com.google.gson.Gson;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SerializationTest
{
    private static final Logger log = LoggerFactory.getLogger(SerializationTest.class);

    @Test
    public void jamSerializationTest()
    {
        double latitude = 48.14564;
        double longitude = 8.21359;

        long timestamp = new Date().getTime();
        UUID id = UUID.randomUUID();

        TrafficJamResource jam = new TrafficJamResource(latitude, longitude, timestamp, id, "testUser1");

        log.debug("Serialized Jam is: " + jam.toJsonObject());
    }
}
