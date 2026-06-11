package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;

import java.util.List;
import java.util.Optional;

/**
 * THe app store stores all connection and form information, the default implementation is backed by a database
 * that is stored in the .tcmenu directory.
 */
public interface AppDataStore {
    /**
     * get a connection object by its ID
     * @param id the id to fetch
     * @return the connection object
     */
    TcMenuPersistedConnection getConnectionById(int id);

    /**
     * All connections objects in a list
     * @return all available connection objects
     */
    List<TcMenuPersistedConnection> getAllConnections();

    /**
     * Updates a connection on the database, if the local ID is -1 it is assumed to be insert mode.
     * @param connection the connection to update
     * @return the localID of the updated/inserted item
     */
    int updateConnection(TcMenuPersistedConnection connection) throws DataException;

    /**
     * Deletes a connection from the database
     * @param connection the connection to delete
     */
    void deleteConnection(TcMenuPersistedConnection connection) throws DataException;

    /**
     * Gets the global embedControl settings from the database
     * @return global settings
     */
    Optional<TcPreferencesPersistence> getGlobalSettings(ApplicationThemeManager themeManager);

    /**
     * Updates the global settings in the database, or inserts if not present.
     * @param settings the settings to update
     */
    void updateGlobalSettings(TcPreferencesPersistence settings) throws DataException;

    TccDatabaseUtilities getUtilities();
}
