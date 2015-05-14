package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.model.TrafficJamDTO;

import java.util.List;
import java.util.Set;

public interface IBeaconDb
{
    TrafficJamDTO getTrafficJam(String id);
    void storeTrafficJam (TrafficJamDTO trafficJam);
    List<TrafficJamDTO> getTrafficJamList();
    void updateTrafficJam (TrafficJamDTO trafficJam);
    void deleteTrafficJam(String id);

    long createUser(String id);
    long deleteUser(String id);
    Set<String> getRegisteredUsers();
}