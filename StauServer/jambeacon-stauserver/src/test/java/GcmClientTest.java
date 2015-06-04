import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Sender;
import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import com.sta.dhbw.stauserver.util.Util;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class GcmClientTest
{

    private static final Logger log = LoggerFactory.getLogger(GcmClientTest.class);

    @Test
    public void gcmSendTest() throws StauserverException, IOException
    {
        List<String> recipients = new ArrayList<>();
        recipients.add("fGTaRZmskC0:APA91bGaT1vgP_45B4qxJl91uVKvA6Twnd9QWTLfF0SlvVwmcoIe2vDWyMn1XmJkWJRr3A1wV7YyWiTM6_7KB6x69TlBksd60VuMVSfS0NMTGI-KUNrg_sYrkWhMDlnT9hWdsEtO-Yq8");
        recipients.add("fx5fUH4hMjs:APA91bFn5U8NY8tpjPLTEXW1TSCgswWDylrVIapZKlcw67feH7y_lIbPLO8snUEYLrZ4VDzOjiJOIUDSVDN-7CXO3zTcSpuHQSyLdQste2aaFLxtthmU65BE9igiBH6YdC0umq0uiHok");

        TrafficJamResource resource = new TrafficJamResource(48.782628, 8.621221, new Date().getTime(), UUID.randomUUID(), "5249911db55f94ad82534e80ff128f3969bdfd0bbba2f670239b72c14d12594c");
        Message message = new Message.Builder()
                .setData(Util.trafficJamToMap(resource))
                .build();

        MulticastResult result = new Sender("AIzaSyD9zwvulCjrJwofmO3ZpLsBbqOyC8Q12Vc").send(message, recipients, 3);
        assertEquals(2, result.getSuccess());

    }
}
