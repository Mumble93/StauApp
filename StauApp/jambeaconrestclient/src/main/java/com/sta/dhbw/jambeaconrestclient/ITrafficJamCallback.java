package com.sta.dhbw.jambeaconrestclient;

import java.util.List;

/**
 * This Interface should be implemented if an activity performs any operations with TrafficJam resources.
 */
public interface ITrafficJamCallback
{
    /**
     * Callback method when a certain TrafficJam resource has been fetched from the server.
     *
     * @param trafficJam The result of the GET operation
     */
    void onGetTrafficJamComplete(TrafficJam trafficJam);

    /**
     * Callback method when the entire dataset of TrafficJam resources has been fetched from the server.
     *
     * @param trafficJamList The {@code List} of TrafficJam objects that was fetched from the server.
     */
    void onGetJamListComplete(List<TrafficJam> trafficJamList);

    /**
     * Callback method when an update operation (PUT) was completed.
     *
     * @param updatedJam The result of the PUT operation, as new TrafficJam
     */
    void onTrafficJamUpdateComplete(TrafficJam updatedJam);

    /**
     * Callback method when a TrafficJam was sent to the server via POST.
     *
     * @param jam The result of the POST operation, as TrafficJam
     */
    void onTrafficJamPostComplete(TrafficJam jam);
}
