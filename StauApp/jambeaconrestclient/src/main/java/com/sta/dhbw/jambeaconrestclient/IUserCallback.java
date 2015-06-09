package com.sta.dhbw.jambeaconrestclient;

/**
 * This Interface should be implemented if an Activity performs operations with User data.
 */
public interface IUserCallback
{
    /**
     * Callback method when a registering (POST) operation completed.
     * @param xRequestId The String contained in the response of the server.
     */
    void onRegisterComplete(String xRequestId);

    /**
     * Callback method when an update (PUT) operation on the user endpoint completed.
     * @param updatedXRequestId The String contained in the response of the server.
     */
    void onUserUpdateComplete(String updatedXRequestId);

    /**
     * Callback method when a DELETE operation on the user endpoint completetd.
     * @param resultCode The Status Code of the server's response.
     */
    void onUserUnregister(Integer resultCode);
}
