package com.sta.dhbw.jambeaconrestclient;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.sta.dhbw.jambeaconrestclient.exception.JamBeaconException;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application>
{
    public ApplicationTest()
    {
        super(Application.class);
    }

    public void heartBeatTest() throws JamBeaconException
    {
        JamBeaconRestClient restClient = new JamBeaconRestClient();
        assertTrue(restClient.serverIsAvailable());
    }
}