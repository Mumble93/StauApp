package com.sta.dhbw.jambeaconrestclient.model;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UserDTO
{
    private String userId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
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
}
