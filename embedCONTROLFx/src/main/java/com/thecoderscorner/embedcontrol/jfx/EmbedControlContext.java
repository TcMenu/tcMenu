package com.thecoderscorner.embedcontrol.jfx;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

public interface EmbedControlContext {
    ScheduledExecutorService getExecutorService();
    JsonMenuItemSerializer getSerializer();
    PlatformSerialFactory getSerialFactory();

    /**
     * Create a new connection from the creator provided.
     * @param connectionCreator the creator object to create the underlying connection
     */
    void createConnection(ConnectionCreator connectionCreator);

    /**
     * Edit the connection by selecting the connection editor panel with a UUID selected. Must be called on UI thread.
     * @param identifier the connection identifier for editing
     */
    void editConnection(UUID identifier);

    /**
     * Delete the connection WITHOUT any user interaction. Must be called on UI thread.
     * @param identifier the connection to completely delete
     */
    void deleteConnection(UUID identifier);
}
