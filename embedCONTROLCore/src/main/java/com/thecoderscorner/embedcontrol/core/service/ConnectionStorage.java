package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * This interface is used mainly by embedCONTROL remote to store all the current connections, providing
 * capabilities to load, save and delete panels.
 * @param <T> the layout persistence
 */
public interface ConnectionStorage<T extends ScreenLayoutPersistence> {
    /**
     * @return a list of panels loaded back from storage that represent each previously open panel
     * @throws IOException if the panels can't be loaded
     */
    List<T> loadAllRemoteConnections() throws IOException;

    /**
     * Saves a panel to storage
     * @param panel the panel to save
     */
    void savePanel(T panel);

    /**
     * Delete a storage item given the ID.
     * @param id the ID to remove
     * @return true if removed
     */
    boolean deletePanel(UUID id);
}
