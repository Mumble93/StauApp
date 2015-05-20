package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;

public interface IBeaconDb
{
    TrafficJamResource getTrafficJam(String id);
    void storeTrafficJam (TrafficJamResource trafficJam) throws StauserverException;
    List<TrafficJamResource> getTrafficJamList();
    void updateTrafficJam (TrafficJamResource trafficJam);
    void deleteTrafficJam(String id) throws NotFoundException;

    long createUser(String id, String hash);
    void deleteUser(String id, String hash) throws NotFoundException;
    boolean userIsRegistered(String hash);
    Set<String> getRegisteredUsers();
    String getRecipientString();
}