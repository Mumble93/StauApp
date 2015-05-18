package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.model.TrafficJamModel;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;

public interface IBeaconDb
{
    TrafficJamModel getTrafficJam(String id);
    void storeTrafficJam (TrafficJamModel trafficJam) throws StauserverException;
    List<TrafficJamModel> getTrafficJamList();
    void updateTrafficJam (TrafficJamModel trafficJam);
    void deleteTrafficJam(String id) throws NotFoundException;

    long createUser(String id, String hash);
    void deleteUser(String id, String hash) throws NotFoundException;
    Set<String> getRegisteredUsers();
}