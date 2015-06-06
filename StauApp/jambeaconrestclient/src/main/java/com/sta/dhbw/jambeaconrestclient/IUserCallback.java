package com.sta.dhbw.jambeaconrestclient;

public interface IUserCallback
{
    void onRegisterComplete(String xRequestId);

    void onUserUpdateComplete(String updatedXRequestId);

    void onUserUnregister(Integer resultCode);
}
