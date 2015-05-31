package com.sta.dhbw.jambeaconrestclient;

import java.util.List;

public interface ICallBackInterface
{
    void onCheckComplete(boolean success);
    void onRegisterComplete(String xRequestId);
    void onUserUpdateComplete(String updatedXRequestId);

    void onGetTrafficJamComplete(TrafficJam trafficJam);
    void onGetJamListComplete(List<TrafficJam> trafficJamList);
    void onTrafficJamUpdateComplete(TrafficJam updatedJam);
}
