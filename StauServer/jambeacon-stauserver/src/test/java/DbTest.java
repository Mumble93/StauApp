import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.TrafficJamModel;
import com.sta.dhbw.stauserver.rest.JamRestServiceEndpoint;
import org.junit.BeforeClass;
import org.junit.Test;

public class DbTest
{
    private static IBeaconDb dao;

    @BeforeClass
    public static void setup()
    {
        dao = new RedisDao();
    }

    @Test
    public void storeJamTest()
    {
        TrafficJamModel model = new TrafficJamModel();
        model.setLongitude(9.060760);
        model.setLatitude(48.727400);
        model.setTimestamp(1431695874);

        JamRestServiceEndpoint restServiceEndpoint = new JamRestServiceEndpoint();
        restServiceEndpoint.postJam("abc123", model);
    }
}
