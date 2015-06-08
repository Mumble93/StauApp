package com.sta.dhbw.stauserver.rest;

import com.google.android.gcm.server.Message;
import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.gcm.GcmClient;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.List;
import java.util.UUID;

/**
 * Supplies the REST-ful endpoint to handle traffic jam resources.
 */
@Path("jams")
public class JamRestServiceEndpoint
{

    @EJB
    private static IBeaconDb dao;

    private static GcmClient gcmClient;

    /**
     * Gets all stored traffic jams.
     *
     * @return 200 with the list of all jams as JSON in the response body, 204 if the list was empty
     */
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

    /**
     * Returns a single traffic jam as resource.
     *
     * @param id The Id of the traffic jam to be returned, as String
     * @return 200 with traffic jam as JSON in response body, 404 if not found
     */
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

        return Response.status(Status.OK).entity(jam).build();

    }

    /**
     * Stores a traffic jam object in the database.<br>
     * Sends a message via the GCM with the values of the stored resource.
     *
     * @param requestId          The value returned when the user registered, as String in the request header
     * @param trafficJamResource The traffic jam to be stored
     * @return 417 if the request header was not set, 401 if the user is not registered, 400 if an Id was already set in the given object or the database operation fails and 201 with
     * created resource as JSON in response body, if operation was successful
     */
    @POST
    @Consumes("application/json")
    @Produces("application/json")
    public Response postJam(@HeaderParam("X-Request-Id") String requestId, TrafficJamResource trafficJamResource)
    {
        if (gcmClient == null)
        {
            gcmClient = new GcmClient(dao);
        }

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
                    return Response.status(Status.BAD_REQUEST).entity(trafficJamResource).build();
                }
                Message message = gcmClient.buildJamMessage(trafficJamResource);
                gcmClient.sendToGcm(message);
                return Response.status(Status.CREATED).entity(trafficJamResource).build();
            }
        }
    }

    /**
     * Updates a resource specified by the given Id. If resource does not exist, it is created.
     *
     * @param requestId          The value returned when the user registered, as String in the request header
     * @param trafficJamResource The resource to be updated
     * @return 417 if the request header was not set, 401 if the user is not registered, and 201 if created or 200 if updated with
     * created resource as JSON in response body, if operation was successful
     * @throws StauserverException
     */
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
            return Response.status(Status.CREATED).entity(trafficJamResource).build();
        }

        //Continue if Traffic Jam was found
        String existingJamOwner = existingJam.getOwner();
        if (existingJamOwner.equals(requestId))
        {
            existingJam.setLatitude(trafficJamResource.getLatitude());
            existingJam.setLongitude(trafficJamResource.getLongitude());
            existingJam.setTimestamp(trafficJamResource.getTimestamp());
            dao.updateTrafficJam(existingJam);
            return Response.status(Status.OK).entity(existingJam).build();
        } else
        {
            return Response.status(Status.UNAUTHORIZED).build();
        }
    }

    /**
     * Deletes a TrafficJamResource specified by the given Id. Sends a delte message via the GCM
     *
     * @param requestId The value returned when the user registered, as String in the request header
     * @param id        The Id of the resource to be deleted as String in the Path
     * @return 417 if the request header was not set, 401 if the user is not registered, 404 if the resource didn't exist, and 200 with
     * created resource as JSON in response body, if operation was successful
     */
    @Path("{id}")
    @DELETE
    public Response deleteTrafficJam(@HeaderParam("X-Request-Id") String requestId, @PathParam("id") String id)
    {
        if (gcmClient == null)
        {
            gcmClient = new GcmClient(dao);
        }

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
            Message message = gcmClient.buildDeleteMessage(id);
            gcmClient.sendToGcm(message);
            return Response.status(Status.OK).build();
        }
    }

}
