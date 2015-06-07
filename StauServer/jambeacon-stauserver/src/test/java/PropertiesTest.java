import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.Assert.*;

public class PropertiesTest
{
    @Test
    public void propertiesTest()
    {
        Properties properties = new Properties();
        InputStream inputStream = PropertiesTest.class.getClassLoader().getResourceAsStream("application-test.properties");

        try
        {
            properties.load(inputStream);
        } catch (IOException e)
        {
            fail(e.getMessage());
        }

        String testProperty = properties.getProperty("someKey");
        int testInt = Integer.parseInt(properties.getProperty("someInt"));
        boolean testBoolean = Boolean.parseBoolean(properties.getProperty("someBoolean"));
        assertEquals("someValue", testProperty);
        assertEquals(42, testInt);
        assertTrue(testBoolean);

    }
}
