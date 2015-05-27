package com.sta.dhbw.stauserver.application;

import com.sta.dhbw.stauserver.db.IBeaconDb;

import javax.ejb.EJB;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

@ApplicationPath("/rest/api/v1")
public class JamServerRestfulService extends Application
{
    @EJB
    private static IBeaconDb dao;

    @GET
    public Response serviceIsRunning()
    {
        if(dao.isAlive())
        {
            return Response.ok().build();
        } else
        {
            return Response.serverError().build();
        }
    }
}
