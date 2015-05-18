import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.TrafficJamModel;
import com.sta.dhbw.stauserver.rest.JamRestServiceEndpoint;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbTest
{
    private static IBeaconDb dao;

    private static final Logger log = LoggerFactory.getLogger(DbTest.class);

    @BeforeClass
    public static void setup()
    {
        dao = new RedisDao();
    }

    @Ignore
    public void storeJamTest()
    {
        TrafficJamModel model = new TrafficJamModel();
        model.setLongitude(9.060760);
        model.setLatitude(48.727400);
        model.setTimestamp(1431695874);

        JamRestServiceEndpoint restServiceEndpoint = new JamRestServiceEndpoint();
        restServiceEndpoint.postJam("abc123", model);
    }

    @Ignore
    public void deleteJamTest()
    {
        JamRestServiceEndpoint restServiceEndpoint = new JamRestServiceEndpoint();
        restServiceEndpoint.deleteTrafficJam("asdfoaif092ue29fjaods0f9ua92hföisdhgg2", "0fcd0d4f-e8b5-4139-b075-f09a4ebd76dd");
    }
}
