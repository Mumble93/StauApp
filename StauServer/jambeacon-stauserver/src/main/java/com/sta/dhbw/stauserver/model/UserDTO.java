package com.sta.dhbw.stauserver.model;

import com.sta.dhbw.stauserver.util.Constants;

import javax.json.Json;
import javax.json.JsonObject;

public class UserDTO
{
    private String userId;

    public UserDTO(){}

    public UserDTO(String userId)
    {
        this.userId = userId;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public JsonObject toJsonObject()
    {
        return Json.createObjectBuilder()
                .add(Constants.USER_ID, getUserId()).build();
    }
}
