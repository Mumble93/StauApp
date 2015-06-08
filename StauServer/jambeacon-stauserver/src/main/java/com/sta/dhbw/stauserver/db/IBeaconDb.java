package com.sta.dhbw.stauserver.db;

import com.sta.dhbw.stauserver.exception.StauserverException;
import com.sta.dhbw.stauserver.resource.TrafficJamResource;

import javax.ws.rs.NotFoundException;
import java.util.List;

/**
 * This Interface defines all methods needed by the REST-ful Service to interact with the database.
 */
public interface IBeaconDb
{
    /**
     * Determines whether a connection to the database could be established or not.
     *
     * @return True, if a connection to the database could be established, False if not.
     */
    boolean isAlive();

    /**
     * Gets a TrafficJamResource with a certain Id.
     *
     * @param id The Id of the TrafficJamResource that should be fetched
     * @return The TrafficJamResource, if found. If not, returns null.
     */
    TrafficJamResource getTrafficJam(String id);

    /**
     * Stores a TrafficJamResource in the database.
     *
     * @param trafficJam The TrafficJamResource object that should be saved.
     * @throws StauserverException
     */
    void storeTrafficJam(TrafficJamResource trafficJam) throws StauserverException;

    /**
     * Gets all TrafficJamResource objects that are currently held in the database.
     *
     * @return A {@code List} of TrafficJamResource objects
     */
    List<TrafficJamResource> getTrafficJamList();

    /**
     * Updates a single TrafficJamResource, determined by the Id of the given parameter.
     *
     * @param trafficJam The TrafficJamResource to update.
     */
    void updateTrafficJam(TrafficJamResource trafficJam);

    /**
     * Updates a single TrafficJamResource, determined by the Id of the given resource.<br>
     * It may happen that the Id of a registered user changes, so when that happens and said user is the owner
     * of a TrafficJamResource, this method will update the owner field accordingly.
     *
     * @param trafficJam  The TrafficJamResource to update.
     * @param updateOwner A boolean to determine whether to update the owner as well.
     */
    void updateTrafficJam(TrafficJamResource trafficJam, boolean updateOwner);

    /**
     * Deletes a TrafficJamResource with the given Id.
     *
     * @param id The Id of the resource to be deleted, as String
     * @throws NotFoundException
     */
    void deleteTrafficJam(String id) throws NotFoundException;

    /**
     * Creates and stores a new user in the database. <br>
     * Note that the returned hash of the Id is only a dummy, which should be replaced with a fully fledged
     * security mechanism in production.
     *
     * @param id   The GCM registration Id, as String
     * @param hash The hash of the Id, if already known, as String
     * @return The hash of the Id, as String
     * @throws StauserverException
     */
    String createUser(String id, String hash) throws StauserverException;

    /**
     * Delete a user from the database.
     *
     * @param id   The Id of the user to be deleted, as String
     * @param hash The hash of the Id, if known
     * @return 1 if the operation was successful, as primitive long
     * @throws StauserverException
     */
    long deleteUser(String id, String hash) throws StauserverException;

    /**
     * Update an existing user. Should create the user if not already exists.
     *
     * @param oldId     The existing Id of the user, as String
     * @param updatedId The new Id of the user, as String
     * @throws StauserverException
     */
    void updateUser(String oldId, String updatedId) throws StauserverException;

    /**
     * Determines whether a user has already registered by checking the existence of the hash in the database.
     *
     * @param hash The hash of the user, as String
     * @return True, if user exists, false if not
     */
    boolean userIsRegistered(String hash);

    /**
     * Gets the Ids of all registered users. This is used to send the messages over the GCM Service
     *
     * @return A {@code List} of Strings, containing the user Ids
     */
    List<String> getRegisteredUsers();
}