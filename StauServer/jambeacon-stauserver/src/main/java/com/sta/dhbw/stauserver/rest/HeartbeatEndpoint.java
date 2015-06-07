package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("heartbeat")
public class HeartbeatEndpoint
{
    @EJB
    IBeaconDb dao;

    @GET
    public Response serviceIsRunning()
    {
        return Response.ok().build();
    }
}
