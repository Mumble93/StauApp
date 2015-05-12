package com.sta.dhbw.stauserver;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

@Path("jams")
public class RestServiceEndpoint
{
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


}
