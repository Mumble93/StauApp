import com.sta.dhbw.stauserver.rest.RestService;
import org.junit.Test;
import org.slf4j.Logger;
import retrofit.RestAdapter;

public class RetrofiTest
{
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(RetrofiTest.class);

    @Test
    public void retrofitTest()
    {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://localhost:8080/rest/api/v1")
                .build();
        RestService restService = restAdapter.create(RestService.class);

        String response = restService.registerUser("retrofitTestUser1");

        log.info(response);
    }

}
