package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.resource.UserResource;
import com.sta.dhbw.stauserver.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import java.security.NoSuchAlgorithmException;

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);
    private IBeaconDb dao = new RedisDao();

    @Path("register")
    @POST
    @Consumes("application/json")
    @Produces("text/plain")
    public Response registerUser(UserResource user) throws NoSuchAlgorithmException
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
            logMessage = "Registered new User with Id: " + userId;
        }
        //User was already contained in set
        else if (result == 0)
        {
            status = Status.CONFLICT;
            logMessage = "User already registered with " + userId;
        }
        //Adding User failed due to other reasons
        else
        {
            status = Status.BAD_REQUEST;
            logMessage = "Error registering new User. Tried with Id: " + userId;
        }

        log.info(logMessage);

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
    public Response unregisterUser(UserResource user) throws NoSuchAlgorithmException
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

        log.info("Unregistered user " + userId);


        return Response.status(status).build();
    }
}
