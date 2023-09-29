package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Provides support functions that can be used by other components within the embedCONTROL app.
 */
public interface EmbedControlContext {
    /**
     * Gets the global executor service.
     * @return the global executor service
     */
    ScheduledExecutorService getExecutorService();

    /**
     * Gets the global instance of the JSON serializer
     * @return the JSON serializer
     */
    JsonMenuItemSerializer getSerializer();

    /**
     * Gets an instance of the serial factory for creating new serial connections
     * @return a serial factory for creating connections
     */
    PlatformSerialFactory getSerialFactory();

    /**
     * Delete the connection WITHOUT any user interaction. Must be called on UI thread.
     * @param connection the connection to completely delete
     */
    void deleteConnection(TcMenuPersistedConnection connection);

    ConnectionCreator connectionFromDescription(TcMenuPersistedConnection connection);

    GlobalSettings getSettings();

    AppDataStore getDataStore();

    void updateConnection(TcMenuPersistedConnection newConnection);
}
