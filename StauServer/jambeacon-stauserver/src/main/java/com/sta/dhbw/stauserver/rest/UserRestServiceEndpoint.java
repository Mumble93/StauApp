package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.UserResource;
import com.sta.dhbw.stauserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.EJB;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.security.NoSuchAlgorithmException;

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);

    @EJB
    private static IBeaconDb dao;

    @Path("register")
    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    public Response registerUser(UserResource user) throws StauserverException
    {
        String userId = user.getUserId();
        String userIdHash = Util.hash256(userId);

        Status status;
        String logMessage;

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


        Response response;
        if (status == Status.CREATED)
        {
            response = Response.status(status).entity(userIdHash).build();
        } else
        {
            response = Response.status(status).build();
        }

        return response;
    }

    @Path("unregister")
    @DELETE
    @Consumes("application/json")
    public Response unregisterUser(UserResource user) throws StauserverException
    {
        String userId = user.getUserId();
        Status status = Status.OK;

        try
        {
            dao.deleteUser(userId, Util.hash256(userId));
        } catch (NotFoundException e)
        {
            status = Status.NOT_FOUND;
        }

        return Response.status(status).build();
    }
}
