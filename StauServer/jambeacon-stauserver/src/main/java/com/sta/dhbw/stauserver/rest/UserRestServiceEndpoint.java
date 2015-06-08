package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * Supplies the REST-ful endpoint for user registration, update, and deletion
 */
@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);

    @EJB
    private static IBeaconDb dao;

    /**
     * Stores a new user with the given GCM registration Id in the database.
     *
     * @param userId The GCM registration Id of the user
     * @return 400 if the id wasn't set or the database operation failed, 201 if the user was successfully stored
     * @throws StauserverException
     */
    @Path("register")
    @POST
    @Produces("text/plain")
    @Consumes("text/plain")
    public Response registerUser(String userId) throws StauserverException
    {
        if (null == userId || userId.isEmpty())
        {
            return Response.status(Status.BAD_REQUEST).build();
        }

        String userIdHash = Util.hash256(userId);

        Status status;
        Response response;

        String result = dao.createUser(userId, userIdHash);
        //User was added to set
        if (null != result && !result.isEmpty())
        {
            status = Status.CREATED;
        }
        //Adding User failed due to other reasons
        else
        {
            status = Status.BAD_REQUEST;
            log.error("Error registering new User. Tried with Id: " + userId);
        }

        if (status == Status.CREATED)
        {
            response = Response.status(status).entity(result).build();
        } else
        {
            response = Response.status(status).build();
        }

        return response;
    }

    /**
     * Updates an existing user. User is created if not exists.
     *
     * @param requestId   The current internal Id of the user
     * @param credentials The old and the new registration Ids, formatted as _oldId_;_newId_
     * @return 200 if operation was successful
     * @throws StauserverException
     */
    @Path("update")
    @PUT
    @Consumes("text/plain")
    @Produces("text/plain")
    public Response updateUser(@HeaderParam("X-Request-Id") String requestId, String credentials) throws StauserverException
    {
        if (null == requestId || requestId.isEmpty())
        {
            return Response.status(Status.EXPECTATION_FAILED).entity("X-Request-Id must be set.").build();
        }

        if (null == credentials || credentials.isEmpty())
        {
            return Response.status(Status.BAD_REQUEST).entity("No credentials received.").build();
        }

        if (dao.userIsRegistered(requestId))
        {
            String[] credentialArray = credentials.split(";");
            if (credentialArray.length > 2)
            {
                return Response.status(Status.BAD_REQUEST).entity("Request must be in format <oldId>;<newId>").build();
            } else if (credentialArray.length == 2)
            {
                String oldId = credentialArray[0];
                String updatedId = credentialArray[1];
                dao.updateUser(oldId, updatedId);
                return Response.ok().entity(Util.hash256(updatedId)).build();
            } else if (credentials.length() == 1)
            {
                String userId = credentialArray[0];
                return registerUser(userId);
            } else
            {
                return Response.status(Status.NOT_ACCEPTABLE).entity("Delivered content not accepted.").build();
            }
        } else
        {
            return Response.status(Status.UNAUTHORIZED).entity("Must register first.").build();
        }
    }


    /**
     * Deletes an existing user
     *
     * @param userId The registration Id of the user to be deleted
     * @return 200 if successful, 404 if not exists, 417 or 500 if failed
     * @throws StauserverException
     */
    @Path("unregister/{id}")
    @DELETE
    public Response unregisterUser(@PathParam("id") String userId) throws StauserverException
    {
        if (null == userId || userId.isEmpty())
        {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Status status;

        long result = dao.deleteUser(userId, Util.hash256(userId));

        switch ((int) result)
        {
            case -1:
                status = Status.EXPECTATION_FAILED;
                break;
            case 0:
                status = Status.NOT_FOUND;
                break;
            case 1:
                status = Status.OK;
                log.info("DELETED User " + userId);
                break;
            default:
                status = Status.INTERNAL_SERVER_ERROR;
        }


        return Response.status(status).build();
    }
}
