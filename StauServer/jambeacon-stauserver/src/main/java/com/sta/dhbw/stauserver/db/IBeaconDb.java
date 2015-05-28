package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Set;

public interface IBeaconDb
{
    boolean isAlive();

    TrafficJamResource getTrafficJam(String id);
    void storeTrafficJam (TrafficJamResource trafficJam) throws StauserverException;
    List<TrafficJamResource> getTrafficJamList();
    void updateTrafficJam (TrafficJamResource trafficJam);
    void updateTrafficJam (TrafficJamResource trafficJam, boolean updateOwner);
    void deleteTrafficJam(String id) throws NotFoundException;

    long createUser(String id, String hash) throws StauserverException;
    long deleteUser(String id, String hash) throws StauserverException;
    void updateUser(String oldId, String updatedId) throws StauserverException;
    boolean userIsRegistered(String hash);
    List<String> getRegisteredUsers();
}