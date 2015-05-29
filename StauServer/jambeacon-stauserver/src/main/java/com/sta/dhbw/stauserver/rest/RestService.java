package com.sta.dhbw.stauserver.rest;


import com.sta.dhbw.stauserver.resource.TrafficJamResource;
import retrofit.client.Response;
import retrofit.http.*;

public interface RestService
{
    @GET("/jams")
    Response getAllTrafficJams();

    @GET("/jams/{id}")
    Response getTrafficJam(@Path("id") String id);

    @POST("/jams")
    Response postTrafficJam(@Body TrafficJamResource trafficJam, @Header("X-Request-Id") String requestId);

    @PUT("/jams")
    Response updateTrafficJam(@Body TrafficJamResource trafficJam, @Header("X-Request-Id") String requestId);

    @POST("/users/register")
    String registerUser(@Body String id);
}
