package com.sta.dhbw.stauserver.application;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * This class is used to hook up the REST-ful Service on Server startup.<br>
 * The basic REST endpoint will be _server-root_/rest/api/v1
 */
@ApplicationPath("/rest/api/v1")
public class JamServerRestfulService extends Application
{
}
