package com.sta.dhbw.jambeaconrestclient;

/**
 * This Interface should be implemented if an activity performs a connectivity check, so the
 * asynchronous operation can notify the calling instance of the result.
 */
public interface IHeartbeatCallback
{
    /**
     * Callback method when an availability check (GET) operation returned.
     *
     * @param success The result of the connectivity check, as boolean.
     */
    void onCheckComplete(boolean success);
}
