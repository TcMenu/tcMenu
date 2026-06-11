package com.thecoderscorner.embedcontrol.core.creators;

import java.util.UUID;

/**
 * Allows various UI elements to get hold of the data provided by the creator
 */
public interface RemotePanelDisplayable {
    /**
     * @return the actual underlying creator
     */
    ConnectionCreator getCreator();

    /**
     * @return the UUID associated with this connection
     */
    UUID getUuid();

    /**
     * @return the name of the connection.
     */
    String getPanelName();
}
