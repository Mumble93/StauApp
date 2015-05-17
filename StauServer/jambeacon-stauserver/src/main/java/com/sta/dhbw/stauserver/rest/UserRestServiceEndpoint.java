package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;
import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.UserModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);
    private IBeaconDb dao = new RedisDao();

    @Path("register")
    @POST
    @Consumes("application/json")
    public Response registerUser(UserModel user)
    {
        String userId = user.getUserId();

        Status status;
        String logMessage;

        long result = dao.createUser(userId, user.getUserIdHash());
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

        return Response.status(status).build();
    }

    @Path("unregister")
    @DELETE
    @Consumes("application/json")
    public Response unregisterUser(UserModel user)
    {
        String userId = user.getUserId();
        Status status = Status.OK;

        try
        {
            dao.deleteUser(userId, user.getUserIdHash());
        } catch (NotFoundException e)
        {
            status = Status.NOT_FOUND;
        }

        log.info("Unregistered user " + userId);


        return Response.status(status).build();
    }
}
