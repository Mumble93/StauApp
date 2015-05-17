package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.model.TrafficJamModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.util.List;
import java.util.UUID;

@Path("jams")
public class JamRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(JamRestServiceEndpoint.class);

    private static final IBeaconDb dao = new RedisDao();

    @GET
    @Produces("application/json")
    public Response getAllJams()
    {
        List<TrafficJamModel> jamList = dao.getTrafficJamList();
        if (jamList.isEmpty())
        {
            return Response.status(Status.NO_CONTENT).build();
        } else
        {
            return Response.status(Status.OK).entity(new GenericEntity<List<TrafficJamModel>>(jamList)
            {
            }).build();
        }
    }

    @Path("{id}")
    @GET
    @Produces("application/json")
    public Response getJam(@PathParam("id") String id)
    {
        TrafficJamModel jam;
        try
        {
            jam = dao.getTrafficJam(id);
        } catch (NotFoundException e)
        {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.OK).entity(Entity.json(jam)).build();

    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postJam(@HeaderParam("X-Request-Id") String requestId, TrafficJamModel trafficJamModel)
    {
        if (requestId == null || requestId.isEmpty())
        {
            return Response.status(Status.EXPECTATION_FAILED).entity("X-Request-Id must be set in header.").build();
        } else
        {
            if(trafficJamModel.getJamId() != null)
            {
                return Response.status(Status.BAD_REQUEST).entity("Id must not be set in post request.").build();
            }

            trafficJamModel.setOwner(requestId);
            trafficJamModel.setJamId(UUID.randomUUID());

            try
            {
                dao.storeTrafficJam(trafficJamModel);
            } catch (StauserverException e)
            {
                return Response.status(Status.BAD_REQUEST).entity(Entity.json(trafficJamModel)).build();
            }
            return Response.status(Status.CREATED).entity(Entity.json(trafficJamModel)).build();
        }
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateJam(@HeaderParam("X-Request-Id") String requestId, TrafficJamModel trafficJamModel) throws StauserverException
    {
        TrafficJamModel existingJam;

        //Try retrieving the existing jam. Adhering to REST specification, call of PUT method on non-existing
        //resources will create a new resource
        try
        {
            existingJam = dao.getTrafficJam(trafficJamModel.getJamId().toString());
        } catch (NotFoundException e)
        {
            trafficJamModel.setOwner(requestId);
            dao.storeTrafficJam(trafficJamModel);
            return Response.status(Status.CREATED).entity(Entity.json(trafficJamModel)).build();
        }

        //Continue if Traffic Jam was found
        String existingJamOwner = existingJam.getOwner();
        if (existingJamOwner.equals(requestId))
        {
            existingJam.setLatitude(trafficJamModel.getLatitude());
            existingJam.setLongitude(trafficJamModel.getLongitude());
            existingJam.setTimestamp(trafficJamModel.getTimestamp());
            dao.updateTrafficJam(existingJam);
            return Response.status(Status.OK).entity(Entity.json(existingJam)).build();
        } else
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

}
