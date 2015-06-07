import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.rest.JamRestServiceEndpoint;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DbTest
{
    private static IBeaconDb dao;

    private static final Logger log = LoggerFactory.getLogger(DbTest.class);

    @BeforeClass
    public static void setup() throws StauserverException
    {
        dao = new RedisDao();
    }

    @Ignore
    public void getJamListTest()
    {
        List<String> userList = dao.getRegisteredUsers();
    }

    @Ignore
    public void storeJamTest() throws StauserverException
    {
        TrafficJamResource model = new TrafficJamResource();
        model.setLongitude(9.060760);
        model.setLatitude(48.727400);
        model.setTimestamp(1431695874);

        JamRestServiceEndpoint restServiceEndpoint = new JamRestServiceEndpoint();
        restServiceEndpoint.postJam("abc123", model);
    }

    @Ignore
    public void deleteJamTest() throws StauserverException
    {
        JamRestServiceEndpoint restServiceEndpoint = new JamRestServiceEndpoint();
        restServiceEndpoint.deleteTrafficJam("asdfoaif092ue29fjaods0f9ua92hföisdhgg2", "0fcd0d4f-e8b5-4139-b075-f09a4ebd76dd");
    }
}
