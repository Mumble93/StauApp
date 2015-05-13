package com.sta.dhbw.stauserver.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.json.Json;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("users")
public class UserRestServiceEndpoint
{
    private static final Logger log = LoggerFactory.getLogger(UserRestServiceEndpoint.class);

    @Path("register")
    @POST
    @Consumes("application/json")
    public void registerUser(Json userJson)
    {

    }
}
