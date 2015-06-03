package com.sta.dhbw.jambeaconrestclient;

import java.util.List;

public interface ITrafficJamCallback
{
    void onGetTrafficJamComplete(TrafficJam trafficJam);

    void onGetJamListComplete(List<TrafficJam> trafficJamList);

    void onTrafficJamUpdateComplete(TrafficJam updatedJam);

    void onTrafficJamPostComplete(TrafficJam jam);
}
