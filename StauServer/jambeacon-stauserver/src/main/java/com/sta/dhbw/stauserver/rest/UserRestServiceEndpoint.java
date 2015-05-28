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

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);

    @EJB
    private static IBeaconDb dao;

    @Path("register/{id}")
    @POST
    @Produces("text/plain")
    public Response registerUser(@PathParam("id") String userId) throws StauserverException
    {
        String userIdHash = Util.hash256(userId);

        Status status;
        Response response;

        long result = dao.createUser(userId, userIdHash);
        //User was added to set
        if (result > 0)
        {
            status = Status.CREATED;
        }
        //User was already contained in set
        else if (result == 0)
        {
            status = Status.CONFLICT;
        }
        //Adding User failed due to other reasons
        else
        {
            status = Status.BAD_REQUEST;
            log.error("Error registering new User. Tried with Id: " + userId);
        }

        if (status == Status.CREATED)
        {
            response = Response.status(status).entity(userIdHash).build();
        } else
        {
            response = Response.status(status).build();
        }

        return response;
    }

    @Path("unregister/{id}")
    @DELETE
    public Response unregisterUser(@PathParam("id") String userId) throws StauserverException
    {
        Status status;
        long result = dao.deleteUser(userId, Util.hash256(userId));

        switch ((int)result)
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
