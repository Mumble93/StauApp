package com.sta.dhbw.stauserver.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.ws.rs.*;

@Path("jams")
public class JamRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(JamRestServiceEndpoint.class);

    @Path("test")
    @GET
    @Produces("text/plain")
    public String testAvailability()
    {
        return "isAvailable";
    }

    @GET
    @Produces("application/json")
    public String getAllJams()
    {
        return "lol";
    }

    @Path("{id}")
    @GET
    @Produces("application/json")
    public String getJam(@PathParam("id") String id)
    {
        return "rofl " +id;
    }

    @POST
    @Consumes("application/json")
    public void postTrafficJam(Json message)
    {

    }


}
