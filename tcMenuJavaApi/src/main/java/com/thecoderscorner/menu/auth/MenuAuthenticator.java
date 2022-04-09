package com.thecoderscorner.menu.auth;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * The authenticator interface that supports the checking of name and UUID pairs. It is used to validate users against
 * the provided name and UUID pair. Optionally, the interface can support adding additional authentication pairs.
 */
public interface MenuAuthenticator {
    enum ManagementCapabilities { NOT_EDITABLE, CAN_REMOVE, CAN_REMOVE_ADD }
    /**
     * Check if the user and UUID pair can connect to this board.
     * @param user the user to check for
     * @param uuid the UUID to check for
     * @return true if the user and UUID are allowed, otherwise false
     */
    boolean authenticate(String user, UUID uuid);

    /**
     * Attempt to add authentication for user and UUID, if it fails to be added false will be returned.
     * @param user the user to add
     * @param uuid the uuid associated with the user
     * @return true if added, otherwise false.
     */
    CompletableFuture<Boolean> addAuthentication(String user, UUID uuid);

    /**
     * Remove the authentication for the given user
     * @param user the user to remove
     */
    void removeAuthentication(String user);

    /**
     * Checks if the provided passcode matches with the security passcode and returns false if it does not match.
     * @param passcode the passcode to check
     * @return true if matching, otherwise false
     */
    boolean doesPasscodeMatch(String passcode);

    /**
     * Indicates how this authenticator can be edited, some don't support any, some remove only.
     * @return how the authenticator can be managed
     */
    ManagementCapabilities managementCapabilities();

    /**
     * Gets a list of all apps/users stored in the system
     * @return the list of users
     */
    List<String> getAllNames();

}
