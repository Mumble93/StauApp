package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.gcm.GcmClient;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;

import javax.ejb.EJB;
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

    @EJB
    private static IBeaconDb dao;

    @EJB
    private static GcmClient gcmClient;

    @GET
    @Produces("application/json")
    public Response getAllJams()
    {
        List<TrafficJamResource> jamList = dao.getTrafficJamList();
        if (jamList.isEmpty())
        {
            return Response.status(Status.NO_CONTENT).build();
        } else
        {
            return Response.status(Status.OK).entity(new GenericEntity<List<TrafficJamResource>>(jamList)
            {
            }).build();
        }
    }

    @Path("{id}")
    @GET
    @Produces("application/json")
    public Response getJam(@PathParam("id") String id)
    {
        TrafficJamResource jam = dao.getTrafficJam(id);
        if (null == jam)
        {
            return Response.status(Status.NOT_FOUND).build();
        }

        return Response.status(Status.OK).entity(Entity.json(jam)).build();

    }

    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postJam(@HeaderParam("X-Request-Id") String requestId, TrafficJamResource trafficJamResource)
    {
        if (requestId == null || requestId.isEmpty())
        {
            return Response.status(Status.EXPECTATION_FAILED).entity("X-Request-Id must be set in header.").build();
        } else if (!dao.userIsRegistered(requestId))
        {
            return Response.status(Status.UNAUTHORIZED).build();
        } else
        {
            {
                if (trafficJamResource.getJamId() != null)
                {
                    return Response.status(Status.BAD_REQUEST).entity("Id must not be set in post request.").build();
                }

                trafficJamResource.setOwner(requestId);
                trafficJamResource.setJamId(UUID.randomUUID());

                try
                {
                    dao.storeTrafficJam(trafficJamResource);
                } catch (StauserverException e)
                {
                    return Response.status(Status.BAD_REQUEST).entity(Entity.json(trafficJamResource)).build();
                }
                return Response.status(Status.CREATED).entity(Entity.json(trafficJamResource)).build();
            }
        }
    }

    @PUT
    @Consumes("application/json")
    @Produces("application/json")
    public Response updateJam(@HeaderParam("X-Request-Id") String requestId, TrafficJamResource trafficJamResource) throws StauserverException
    {
        if (null == requestId || requestId.isEmpty())
        {
            return Response.status(Status.EXPECTATION_FAILED).entity("X-Request-Id must be set in header.").build();
        } else if (!dao.userIsRegistered(requestId))
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        TrafficJamResource existingJam;

        //Try retrieving the existing jam. Adhering to REST specification, call of PUT method on non-existing
        //resources will create a new resource
        existingJam = dao.getTrafficJam(trafficJamResource.getJamId().toString());

        if (null == existingJam)
        {
            trafficJamResource.setOwner(requestId);
            dao.storeTrafficJam(trafficJamResource);
            return Response.status(Status.CREATED).entity(Entity.json(trafficJamResource)).build();
        }

        //Continue if Traffic Jam was found
        String existingJamOwner = existingJam.getOwner();
        if (existingJamOwner.equals(requestId))
        {
            existingJam.setLatitude(trafficJamResource.getLatitude());
            existingJam.setLongitude(trafficJamResource.getLongitude());
            existingJam.setTimestamp(trafficJamResource.getTimestamp());
            dao.updateTrafficJam(existingJam);
            return Response.status(Status.OK).entity(Entity.json(existingJam)).build();
        } else
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    @Path("{id}")
    @DELETE
    public Response deleteTrafficJam(@HeaderParam("X-Request-Id") String requestId, @PathParam("id") String id)
    {
        if (null == requestId || requestId.isEmpty())
        {
            return Response.status(Status.EXPECTATION_FAILED).entity("X-Request-Id header must be set.").build();
        } else if (!dao.userIsRegistered(requestId))
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }

        TrafficJamResource jam = dao.getTrafficJam(id);

        if (null == jam)
        {
            return Response.status(Status.NOT_FOUND).build();
        } else if (!jam.getOwner().equals(requestId))
        {
            return Response.status(Status.UNAUTHORIZED).build();
        } else
        {
            dao.deleteTrafficJam(id);
            return Response.status(Status.OK).build();
        }
    }

}
