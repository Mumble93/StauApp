package com.sta.dhbw.stauserver.rest;

import com.sta.dhbw.stauserver.db.IBeaconDb;

import javax.ejb.EJB;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Supplies the heartbeat endpoint used to determine if the app is able to reach the server.
 */
@Path("heartbeat")
public class HeartbeatEndpoint
{
    @EJB
    IBeaconDb dao;

    /**
     * Determines whether the database is reachable.
     *
     * @return Returns an empty 200 response, If the database is connected, 503 (Service Unavailable) if not
     */
    @GET
    public Response serviceIsRunning()
    {
        return Response.ok().build();
    }
}
