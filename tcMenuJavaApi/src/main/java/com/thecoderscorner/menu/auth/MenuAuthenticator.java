package com.thecoderscorner.menu.auth;

import java.util.UUID;

/**
 * The authenticator interface that supports the checking of name and UUID pairs. It is used to validate users against
 * the provided name and UUID pair. Optionally, the interface can support adding additional authentication pairs.
 */
public interface MenuAuthenticator {
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
    boolean addAuthentication(String user, UUID uuid);
}
