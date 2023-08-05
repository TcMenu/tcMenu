package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.menu.domain.state.PortableColor;
import org.springframework.jdbc.core.JdbcTemplate;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection.StoreConnectionType;

/**
 * Creates a connection with the local SQLITE database stored in the .tcmenu directory and provides the core config
 * for both embedControl and other key tables.
 */
public class DatabaseAppDataStore implements AppDataStore {
    private static System.Logger logger = System.getLogger(DatabaseAppDataStore.class.getSimpleName());
    private final JdbcTemplate template;
    private static final String CREATE_SCHEMA_VERSION = """
        CREATE TABLE TC_SCHEMA_VERSION(SCHEMA_VERSION INT)
        """;
    private static final String CREATE_GLOBAL_SETTINGS_TABLE = """
        CREATE TABLE GLOBAL_SETTINGS(
            SETTING_ID INT PRIMARY KEY,
            LOCAL_NAME VARCHAR(64),
            LOCAL_UUID VARCHAR(64),
            RECURSE_SUB INT,
            FONT_SIZE INT,
            BUTTON_FG VARCHAR(16),
            BUTTON_BG VARCHAR(16),
            UPDATE_FG VARCHAR(16),
            UPDATE_BG VARCHAR(16),
            HIGHLIGHT_FG VARCHAR(16),
            HIGHLIGHT_BG VARCHAR(16),
            TEXT_FG VARCHAR(16),
            TEXT_BG VARCHAR(16),
            ERROR_FG VARCHAR(16),
            ERROR_BG VARCHAR(16),
            DIALOG_FG VARCHAR(16),
            DIALOG_BG VARCHAR(16)
        )
    """;
    private static final String CREATE_CONNECTIONS_TABLE = """
        CREATE TABLE TC_CONNECTION(
            LOCAL_ID INT PRIMARY KEY,
            CONNECTION_NAME VARCHAR(64),
            CONNECTION_UUID VARCHAR(64),
            CONNECTION_TYPE VARCHAR(32),
            HOST_OR_SERIAL_ID VARCHAR(64),
            PORT_OR_BAUD VARCHAR(20),
            EXTRA_DATA BLOB,
            SELECTED_FORM VARCHAR(128),
            LAST_MODIFIED VARCHAR(64)
        )
        """;
    private static final String CREATE_FORM_STORE_TABLE = """
            CREATE TABLE TC_FORM(
                FORM_ID INT PRIMARY KEY,
                FORM_UUID VARCHAR(64),
                FORM_NAME VARCHAR(128),
                XML_DATA BLOB
            )
            """;

    private final static int EXPECTED_SCHEMA = 1;

    public DatabaseAppDataStore(JdbcTemplate template) {
        this.template = template;

        int schemaVersion = getSchemaVersion();
        if(schemaVersion < EXPECTED_SCHEMA) {
           throw new IllegalArgumentException("Schema version of database is less than " + EXPECTED_SCHEMA);
        }
    }

    public TcMenuPersistedConnection getConnectionById(int id) {
        ensureTableExists(template, "TC_CONNECTION", CREATE_CONNECTIONS_TABLE);
        return template.queryForObject("SELECT * from TC_CONNECTION where LOCAL_ID = ?", this::mapConnectionObject, id);
    }

    private TcMenuPersistedConnection mapConnectionObject(ResultSet resultSet, int i) throws SQLException {
        return new TcMenuPersistedConnection(
                resultSet.getInt("LOCAL_ID"),
                resultSet.getString("CONNECTION_NAME"),
                resultSet.getString("CONNECTION_UUID"),
                resultSet.getString("SELECTED_FORM"),
                StoreConnectionType.valueOf(resultSet.getString("CONNECTION_TYPE")),
                resultSet.getString("HOST_OR_SERIAL_ID"),
                resultSet.getString("PORT_OR_BAUD"),
                resultSet.getString("EXTRA_DATA"),
                resultSet.getString("LAST_MODIFIED"),
                this
        );
    }

    public List<TcMenuPersistedConnection> getAllConnections() {
        ensureTableExists(template, "TC_CONNECTION", CREATE_CONNECTIONS_TABLE);
        return template.query("SELECT * FROM TC_CONNECTION", this::mapConnectionObject);
    }

    public int updateConnection(TcMenuPersistedConnection connection) {
        int localId = connection.getLocalId() == -1 ? findNextConnectionId() : connection.getLocalId();
        var params = new Object[]{
                connection.getName(),
                connection.getFormName(),
                connection.getUuid(),
                connection.getConnectionType(),
                connection.getHostOrSerialId(),
                connection.getPortOrBaud(),
                connection.getExtraData(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME),
                localId
        };

        var count = template.queryForObject("SELECT COUNT(*) from TC_CONNECTION where LOCAL_ID = ?", Integer.class, connection.getLocalId());
        if(count == null || count == 0) {
            logger.log(System.Logger.Level.INFO, "Insert tc connection " + connection);
            template.update("""
                INSERT INTO TC_CONNECTION(
                    CONNECTION_NAME, SELECTED_FORM, CONNECTION_UUID, CONNECTION_TYPE, 
                    HOST_OR_SERIAL_ID, PORT_OR_BAUD, EXTRA_DATA, LAST_MODIFIED, LOCAL_ID) 
                values(?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, params);
        } else {
            logger.log(System.Logger.Level.INFO, "Update tc connection " + connection);
            template.update("""
                UPDATE TC_CONNECTION
                SET CONNECTION_NAME = ?, SELECTED_FORM = ?, CONNECTION_UUID = ?, CONNECTION_TYPE = ?, 
                    HOST_OR_SERIAL_ID = ?, PORT_OR_BAUD = ?, EXTRA_DATA = ?, LAST_MODIFIED = ?
                WHERE LOCAL_ID = ?
                """, params);
        }
        return localId;
    }

    private int findNextConnectionId() {
        var res = template.queryForObject("SELECT MAX(LOCAL_ID) from TC_CONNECTION", Integer.class);
        return (res == null || res == 0) ? 1 : (res + 1);
    }

    public void deleteConnection(TcMenuPersistedConnection connection) {
        template.update("DELETE FROM TC_CONNECTION WHERE LOCAL_ID = ?", connection.getLocalId());
    }

    public GlobalSettings getGlobalSettings() {
        logger.log(System.Logger.Level.INFO, "Get global settings from store");
        try {
            if(!ensureTableExists(template, "GLOBAL_SETTINGS", CREATE_GLOBAL_SETTINGS_TABLE)) {
                logger.log(System.Logger.Level.INFO, "Setting up global setting table with defaults");
                updateGlobalSettings(new GlobalSettings());
            }
            logger.log(System.Logger.Level.INFO, "Reading settings from table");
            return template.queryForObject("SELECT * FROM GLOBAL_SETTINGS where SETTING_ID=0", this::settingsMapper);
        } catch(Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Unable to query global settings", ex);
            return new GlobalSettings();
        }
    }

    private GlobalSettings settingsMapper(ResultSet resultSet, int i) throws SQLException {
        var settings = new GlobalSettings();
        settings.setAppName(resultSet.getString("LOCAL_NAME"));
        settings.setAppUuid(resultSet.getString("LOCAL_UUID"));
        settings.setDefaultRecursiveRendering(resultSet.getInt("RECURSE_SUB") != 0);
        settings.setDefaultFontSize(resultSet.getInt("FONT_SIZE"));
        settings.getButtonColor().setFg(new PortableColor(resultSet.getString("BUTTON_FG")));
        settings.getButtonColor().setBg(new PortableColor(resultSet.getString("BUTTON_BG")));
        settings.getUpdateColor().setFg(new PortableColor(resultSet.getString("UPDATE_FG")));
        settings.getUpdateColor().setBg(new PortableColor(resultSet.getString("UPDATE_BG")));
        settings.getHighlightColor().setFg(new PortableColor(resultSet.getString("HIGHLIGHT_FG")));
        settings.getHighlightColor().setBg(new PortableColor(resultSet.getString("HIGHLIGHT_BG")));
        settings.getTextColor().setFg(new PortableColor(resultSet.getString("TEXT_FG")));
        settings.getTextColor().setBg(new PortableColor(resultSet.getString("TEXT_BG")));
        settings.getErrorColor().setFg(new PortableColor(resultSet.getString("ERROR_FG")));
        settings.getErrorColor().setBg(new PortableColor(resultSet.getString("ERROR_BG")));
        settings.getDialogColor().setFg(new PortableColor(resultSet.getString("DIALOG_FG")));
        settings.getDialogColor().setBg(new PortableColor(resultSet.getString("DIALOG_BG")));
        return settings;
    }

    public void updateGlobalSettings(GlobalSettings settings) {
        var parameterList = new Object[] {
                settings.getAppName(), settings.getAppUuid(), settings.isDefaultRecursiveRendering(),
                settings.getDefaultFontSize(),
                settings.getButtonColor().getFg().toString(),
                settings.getButtonColor().getBg().toString(),
                settings.getUpdateColor().getFg().toString(),
                settings.getUpdateColor().getBg().toString(),
                settings.getHighlightColor().getFg().toString(),
                settings.getHighlightColor().getBg().toString(),
                settings.getTextColor().getFg().toString(),
                settings.getTextColor().getBg().toString(),
                settings.getErrorColor().getFg().toString(),
                settings.getErrorColor().getBg().toString(),
                settings.getDialogColor().getFg().toString(),
                settings.getDialogColor().getBg().toString()
        };

        var res = template.queryForObject("SELECT COUNT(*) FROM GLOBAL_SETTINGS WHERE SETTING_ID=0", Integer.class);
        if(res == null || res ==0) {
            logger.log(System.Logger.Level.INFO, "Insert ec global settings " + settings);

            template.update("""
                   INSERT INTO GLOBAL_SETTINGS(
                        SETTING_ID, LOCAL_NAME, LOCAL_UUID, RECURSE_SUB, FONT_SIZE, BUTTON_FG, BUTTON_BG,
                        UPDATE_FG, UPDATE_BG, HIGHLIGHT_FG, HIGHLIGHT_BG, TEXT_FG, TEXT_BG,
                        ERROR_FG, ERROR_BG, DIALOG_FG, DIALOG_BG)
                   values(0, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                   """, parameterList);
        } else {
            logger.log(System.Logger.Level.INFO, "Update ec global settings " + settings);
            template.update("""
                    UPDATE GLOBAL_SETTINGS
                    SET LOCAL_NAME = ?, LOCAL_UUID = ?, RECURSE_SUB = ?, FONT_SIZE = ?,
                        BUTTON_FG=?, BUTTON_BG=?, UPDATE_FG=?, UPDATE_BG=?,  
                        HIGHLIGHT_FG=?, HIGHLIGHT_BG=?, TEXT_FG=?, TEXT_BG=?,  
                        ERROR_FG=?, ERROR_BG=?, DIALOG_FG=?, DIALOG_BG=?
                    WHERE SETTING_ID = 0;
                    """, parameterList);
        }
    }

    public int getSchemaVersion() {
        if(!ensureTableExists(template, "TC_SCHEMA_VERSION", CREATE_SCHEMA_VERSION)) {
            template.update("INSERT INTO TC_SCHEMA_VERSION(SCHEMA_VERSION) VALUES(" + EXPECTED_SCHEMA + ")");
        }

        var ret = template.queryForObject("SELECT SCHEMA_VERSION FROM TC_SCHEMA_VERSION", Integer.class);
        if(ret == null) throw new IllegalArgumentException("Schema could not be queried");
        return ret;
    }

    @Override
    public List<TcMenuFormPersistence> getAllForms() {
        ensureTableExists(template, "TC_FORM", CREATE_FORM_STORE_TABLE);
        return template.query("SELECT FORM_ID, FORM_UUID, FORM_NAME from TC_FORM", this::mapSqlToForm);
    }

    @Override
    public List<TcMenuFormPersistence> getAllFormsForUuid(String uuid) {
        ensureTableExists(template, "TC_FORM", CREATE_FORM_STORE_TABLE);
        return template.query("SELECT FORM_ID, FORM_UUID, FORM_NAME from TC_FORM where FORM_UUID = ?", this::mapSqlToForm, uuid);
    }

    @Override
    public void updateForm(TcMenuFormPersistence form) {
        ensureTableExists(template, "TC_FORM", CREATE_FORM_STORE_TABLE);
        if(form.getFormId() == -1) {
            var res = template.queryForObject("SELECT MAX(FORM_ID) FROM TC_FORM", Integer.class);
            if(res == null) res = 0;
            template.update("INSERT INTO TC_FORM(FORM_ID, FORM_UUID, FORM_NAME, XML_DATA) values(?,?,?,?)",
                    res + 1, form.getUuid(), form.getFormName(), form.getXmlData());
        } else {
            template.update("UPDATE TC_FORM set XML_DATA=?, FORM_UUID=?, FORM_NAME=? where FORM_ID=?",
                    form.getXmlData(), form.getUuid(), form.getFormName(), form.getFormId());
        }
    }

    @Override
    public void deleteForm(TcMenuFormPersistence form) {
        ensureTableExists(template, "TC_FORM", CREATE_FORM_STORE_TABLE);
        template.update("DELETE FROM TC_FORM WHERE FORM_ID=?", form.getFormId());
    }

    @Override
    public String getUniqueFormData(int formId) {
        return template.queryForObject("SELECT XML_DATA FROM TC_FORM WHERE FORM_ID=?",
                String.class, formId);
    }

    private TcMenuFormPersistence mapSqlToForm(ResultSet resultSet, int i) throws SQLException {
        return new TcMenuFormPersistence(
                resultSet.getInt("FORM_ID"),
                resultSet.getString("FORM_UUID"),
                resultSet.getString("FORM_NAME"),
                this
        );
    }

    public static boolean ensureTableExists(JdbcTemplate template, String table, String ddlToCreate) {
        logger.log(System.Logger.Level.DEBUG, "Checking for table " + table);
        var sql = "SELECT COUNT(name) FROM sqlite_master WHERE type='table' AND name=?";
        var res = template.queryForObject(sql, Integer.class, table);
        if(res == null || res == 0) {
            logger.log(System.Logger.Level.DEBUG, "Creating table " + table);
            template.execute(ddlToCreate);
            return false;
        }
        return true;
    }
}
