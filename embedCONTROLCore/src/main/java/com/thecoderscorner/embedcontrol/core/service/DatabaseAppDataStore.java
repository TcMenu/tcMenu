package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;

import java.util.List;
import java.util.Optional;

/**
 * Creates a connection with the local database stored in the .tcmenu directory and provides the core config
 * for both embedControl and other key tables.
 */
public class DatabaseAppDataStore implements AppDataStore {
    private static final System.Logger logger = System.getLogger(DatabaseAppDataStore.class.getSimpleName());
    private final TccDatabaseUtilities databaseHelper;

    public DatabaseAppDataStore(TccDatabaseUtilities databaseHelper) {
        this.databaseHelper = databaseHelper;
        try {
            databaseHelper.ensureTableFormatCorrect(
                    TcMenuPersistedConnection.class,
                    TcPreferencesPersistence.class,
                    TcPreferencesColor.class
            );
        } catch (DataException e) {
            logger.log(System.Logger.Level.ERROR, "Could not update database to new schema", e);
        }
    }

    public TcMenuPersistedConnection getConnectionById(int id) {
        try {
            return databaseHelper.queryPrimaryKey(TcMenuPersistedConnection.class, id).orElseThrow();
        } catch (DataException e) {
            logger.log(System.Logger.Level.ERROR, "GetConnection failed for " + id, e);
            return null;
        }
    }

    public List<TcMenuPersistedConnection> getAllConnections() {
        try {
            return databaseHelper.queryRecords(TcMenuPersistedConnection.class, "");
        } catch (DataException e) {
            logger.log(System.Logger.Level.ERROR, "GetAllConnections failed", e);
            return List.of();
        }
    }

    public int updateConnection(TcMenuPersistedConnection connection) throws DataException {
        return databaseHelper.updateRecord(TcMenuPersistedConnection.class, connection);
    }

    public void deleteConnection(TcMenuPersistedConnection connection) throws DataException {
        databaseHelper.executeRaw("DELETE FROM TC_CONNECTION WHERE LOCAL_ID = ?", connection.getLocalId());
    }

    public Optional<TcPreferencesPersistence> getGlobalSettings(ApplicationThemeManager themeManager) {
        logger.log(System.Logger.Level.INFO, "Get global settings from store");
        try {
            var maybeSettings = databaseHelper.queryPrimaryKey(TcPreferencesPersistence.class, 0);
            if(maybeSettings.isEmpty()) {
                logger.log(System.Logger.Level.INFO, "Setting up global setting table with defaults");
                GlobalSettings settings = new GlobalSettings(themeManager);
                updateGlobalSettings(new TcPreferencesPersistence(settings));
                logger.log(System.Logger.Level.INFO, "Reading settings from table");
                maybeSettings = databaseHelper.queryPrimaryKey(TcPreferencesPersistence.class, 0);
            }
            maybeSettings.orElseThrow().setColorsToSave(databaseHelper.queryRecords(TcPreferencesColor.class, ""));
            return maybeSettings;
        } catch(Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Unable to query global settings", ex);
            return Optional.empty();
        }
    }

    public void updateGlobalSettings(TcPreferencesPersistence settings) throws DataException {
        databaseHelper.updateRecord(TcPreferencesPersistence.class, settings);
        if(settings.getColorsToSave() != null) {
            for (var col : settings.getColorsToSave()) {
                databaseHelper.updateRecord(TcPreferencesColor.class, col);
            }
        }
    }

    @Override
    public TccDatabaseUtilities getUtilities() {
        return databaseHelper;
    }

}
