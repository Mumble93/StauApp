package com.sta.dhbw.jambeaconrestclient.model;

import com.sta.dhbw.jambeaconrestclient.util.Constants;

import javax.json.Json;
import javax.json.JsonObject;

public class UserDTO
{
    private String userId;
    private String userIdHash;

    public UserDTO(){}

    public UserDTO(String userId, String userIdHash)
    {
        this.userId = userId;
        this.userIdHash = userIdHash;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getUserIdHash()
    {
        return userIdHash;
    }

    public void setUserIdHash(String userIdHash)
    {
        this.userIdHash = userIdHash;
    }

    public JsonObject toJsonObject()
    {
        return Json.createObjectBuilder()
                .add(Constants.USER_ID, getUserId()).build();
    }
}
