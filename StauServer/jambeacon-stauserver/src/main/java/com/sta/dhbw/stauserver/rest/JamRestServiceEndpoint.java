package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.model.TrafficJamDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("jams")
public class JamRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(JamRestServiceEndpoint.class);

    @GET
    @Produces("application/json")
    public List<TrafficJamDTO> getAllJams()
    {
        return null;
    }

    @Path("{id}")
    @GET
    @Produces("application/json")
    public TrafficJamDTO getJam(@PathParam("id") String id)
    {
        return null;
    }

    @POST
    @Consumes("application/json")
    public Response postTrafficJam(TrafficJamDTO trafficJamDTO)
    {
        return null;
    }


}
