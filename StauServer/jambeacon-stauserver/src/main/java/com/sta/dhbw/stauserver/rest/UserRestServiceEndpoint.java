package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.RedisDao;
import com.sta.dhbw.stauserver.model.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);
    private RedisDao dao = new RedisDao();

    @Path("register")
    @POST
    @Consumes("application/json")
    public Response registerUser(UserDTO user)
    {
        String userId = user.getUserId();

        Status status;
        String logMessage;

        long result = dao.createUser(userId);
        if (result >= 0)
        {
            status = Status.CREATED;
            logMessage = "Registered new User with Id: " + userId;
        } else
        {
            status = Status.BAD_REQUEST;
            logMessage = "Error registering new User. Tried with Id: " + userId;
        }

        log.info(logMessage);

        return Response.status(status).build();
    }

    @Path("unregister/{userId}")
    @DELETE
    public Response unregisterUser(@PathParam("userId") String userId)
    {
        long result = dao.deleteUser(userId);
        Status status;
        if (1 == result)
        {
            status = Status.OK;
            log.info("Unregistered user " + userId);
        } else
        {
            status = Status.NOT_FOUND;
        }
        return Response.status(status).build();
    }
}
