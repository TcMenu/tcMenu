package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.embedcontrol.core.service.DatabaseAppDataStore;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.persist.ReleaseType;
import com.thecoderscorner.menu.persist.VersionInfo;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.InputStream;
import java.lang.System.Logger.Level;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.prefs.Preferences;

import static com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage.BUILD_TIMESTAMP_KEY;
import static com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage.BUILD_VERSION_KEY;
import static java.lang.System.Logger.Level.*;

public class JdbcTcMenuConfigurationStore implements ConfigurationStorage {
    private final Properties props = new Properties();

    private final static String CREATE_RECENTS_SQL = """
            CREATE TABLE TC_MENU_RECENTS(
                RECENT_FILE VARCHAR(255),
                RECENT_IDX INT
            )
            """;

    private final static String CREATE_ADD_PLUGIN_SQL = """
            CREATE TABLE TC_MENU_EXTRA_PLUGINS(
                PLUGIN_PATH VARCHAR(255),
                PLUGIN_IDX INT
            )
            """;
    private final static String CREATE_SETTINGS_SQL = """
            CREATE TABLE TC_MENU_SETTINGS(
                SETTING_ID INT PRIMARY KEY,
                DEFAULT_RECURSIVE INT,
                CHOSEN_LOCALE VARCHAR(32),
                ARDUINO_OVERRIDE VARCHAR(255),
                LIBRARIES_OVERRIDE VARCHAR(255),
                DEFAULT_SAVE_LOCATION INT,
                DEFAULT_SIZED_EEPROM INT,
                LAST_LOADED_PROJECT VARCHAR(255),
                LAST_RUN_VERSION VARCHAR(20),
                USING_ARDUINO_IDE INT,
                MAX_SCAN_PROJECT_LEVEL INT,
                NUM_BACKUPS INT,
                REGISTERED_KEY VARCHAR(256),
                RELEASE_STREAM INT,
                CURRENT_THEME VARCHAR(32)
            )
            """;
    private final System.Logger logger = System.getLogger(JdbcTcMenuConfigurationStore.class.getSimpleName());
    private final JdbcTemplate jdbcTemplate;
    private final LoadedConfiguration loadedConfig;
    private boolean autoCommit = true;
    private ArduinoDirectoryChangeListener arduinoChangeListener = null;

    public JdbcTcMenuConfigurationStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        LoadedConfiguration loaded;
        try {
            createIfNeeded();
            loaded = jdbcTemplate.queryForObject("SELECT * FROM TC_MENU_SETTINGS", this::mapSettings);
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            props.load( resourceAsStream );
        } catch(Exception ex) {
            loaded = new LoadedConfiguration();
        }
        loadedConfig = loaded;
        logger.log(INFO, "Loaded initial designer settings as" + loadedConfig);
    }

    private LoadedConfiguration mapSettings(ResultSet resultSet, int i) throws SQLException {
        var loadedConfig = new LoadedConfiguration();
        loadedConfig.setRecursiveNaming(resultSet.getInt("DEFAULT_RECURSIVE") != 0);
        loadedConfig.setLocale(resultSet.getString("CHOSEN_LOCALE"));
        loadedConfig.setArduinoOverrideDirectory(resultSet.getString("ARDUINO_OVERRIDE"));
        loadedConfig.setLibraryOverrideDirectory(resultSet.getString("LIBRARIES_OVERRIDE"));
        loadedConfig.setSaveToSrc(resultSet.getInt("DEFAULT_SAVE_LOCATION") !=0 );
        loadedConfig.setEEPROMSized(resultSet.getInt("DEFAULT_SIZED_EEPROM") !=0 );
        loadedConfig.setUsingArduinoIDE(resultSet.getInt("USING_ARDUINO_IDE") !=0 );
        loadedConfig.setLastLoadedProject(resultSet.getString("LAST_LOADED_PROJECT"));
        loadedConfig.setLastRunVersion(resultSet.getString("LAST_RUN_VERSION"));
        loadedConfig.setProjectMaxLevel(resultSet.getInt("MAX_SCAN_PROJECT_LEVEL"));
        loadedConfig.setNumberBackups(resultSet.getInt("NUM_BACKUPS"));
        loadedConfig.setRegisteredKey(resultSet.getString("REGISTERED_KEY"));
        loadedConfig.setReleaseStream(ReleaseType.valueOf(resultSet.getString("RELEASE_STREAM")));
        loadedConfig.setCurrentTheme(resultSet.getString("CURRENT_THEME"));
        return loadedConfig;
    }

    private void createIfNeeded() {
        if(!DatabaseAppDataStore.ensureTableExists(jdbcTemplate, "TC_MENU_SETTINGS", CREATE_SETTINGS_SQL)) {
            logger.log(INFO, "Created table for designer settings");
            // we are creating a new instance, let us try and copy from preferences.
            Preferences prefsNode = Preferences.userNodeForPackage(BaseDialogSupport.class);
            var prefs = new PrefsConfigurationStorage();
            var lastTheme = prefsNode.get("uiTheme", "lightMode");
            logger.log(INFO, "Read preferences from legacy to update");

            saveUniqueRecents(prefs.loadRecents());
            setAdditionalPluginPaths(prefs.getAdditionalPluginPaths());

            jdbcTemplate.update("""
                        INSERT INTO TC_MENU_SETTINGS(
                            DEFAULT_RECURSIVE, CHOSEN_LOCALE, ARDUINO_OVERRIDE, LIBRARIES_OVERRIDE,
                            DEFAULT_SAVE_LOCATION, DEFAULT_SIZED_EEPROM, LAST_LOADED_PROJECT, LAST_RUN_VERSION,
                            USING_ARDUINO_IDE, MAX_SCAN_PROJECT_LEVEL, NUM_BACKUPS, REGISTERED_KEY, RELEASE_STREAM,
                            CURRENT_THEME, SETTING_ID
                        ) values(
                            ?,?,?,?,?,?,?,?,?,?,?,?,?,?,0
                        )
                        """,
                    prefs.isDefaultRecursiveNamingOn(),
                    prefs.getChosenLocale(),
                    prefs.getArduinoOverrideDirectory().orElse(null),
                    prefs.getArduinoLibrariesOverrideDirectory().orElse(null),
                    prefs.isDefaultSaveToSrcOn(),
                    prefs.isDefaultSizedEEPROMStorage(),
                    prefs.getLastLoadedProject().orElse(null),
                    prefs.getLastRunVersion(),
                    prefs.isUsingArduinoIDE(),
                    prefs.getMenuProjectMaxLevel(),
                    prefs.getNumBackupItems(),
                    prefs.getRegisteredKey(),
                    ReleaseType.STABLE,
                    lastTheme
            );
            logger.log(INFO, "Initial state of designer settings persisted");

        } else {
            logger.log(INFO, "Designer settings table existed");
        }
    }

    @Override
    public List<String> loadRecents() {
        DatabaseAppDataStore.ensureTableExists(jdbcTemplate, "TC_MENU_RECENTS", CREATE_RECENTS_SQL);
        var r = jdbcTemplate.query("SELECT RECENT_FILE FROM TC_MENU_RECENTS ORDER BY RECENT_IDX", this::mapRecent);
        logger.log(INFO, "Loaded recents as" + r);
        return r;
    }

    private String mapRecent(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getString("RECENT_FILE");
    }

    @Override
    public void saveUniqueRecents(List<String> recents) {
        try {
            jdbcTemplate.update("DELETE FROM TC_MENU_RECENTS");
            int i = 0;
            for (var recent : recents) {
                jdbcTemplate.update("INSERT INTO TC_MENU_RECENTS(RECENT_FILE, RECENT_IDX) values(?,?)",
                        recent, i++);
            }
            logger.log(INFO, "Saved recents as" + recents);

        } catch (Exception ex) {
            logger.log(Level.ERROR, "Could not write recent entries", ex);
        }
    }

    @Override
    public String getRegisteredKey() {
        return loadedConfig.getRegisteredKey();
    }

    @Override
    public void setRegisteredKey(String registeredKey) {
        loadedConfig.setRegisteredKey(registeredKey);
        saveIfNeeded();
    }

    private void saveIfNeeded() {
        if(autoCommit && loadedConfig.isChanged()) {
            logger.log(INFO, "Saving designer settings" + loadedConfig);
            jdbcTemplate.update("""
                UPDATE TC_MENU_SETTINGS
                SET DEFAULT_RECURSIVE=?,
                    CHOSEN_LOCALE = ?,
                    ARDUINO_OVERRIDE = ?, 
                    LIBRARIES_OVERRIDE = ?,
                    DEFAULT_SAVE_LOCATION = ?, 
                    DEFAULT_SIZED_EEPROM = ?, 
                    LAST_LOADED_PROJECT = ?, 
                    LAST_RUN_VERSION = ?,
                    USING_ARDUINO_IDE = ?,
                    MAX_SCAN_PROJECT_LEVEL = ?, 
                    NUM_BACKUPS = ?, 
                    REGISTERED_KEY = ?,
                    RELEASE_STREAM = ?,
                    CURRENT_THEME = ?
                WHERE SETTING_ID = 0
                """,
                    loadedConfig.isRecursiveNaming() ? 1 : 0,
                    loadedConfig.getLocale(),
                    loadedConfig.getArduinoOverrideDirectory(),
                    loadedConfig.getLibraryOverrideDirectory(),
                    loadedConfig.isSaveToSrc() ? 1 : 0,
                    loadedConfig.isEEPROMSized() ? 1 : 0,
                    loadedConfig.getLastLoadedProject(),
                    loadedConfig.getLastRunVersion(),
                    loadedConfig.isUsingArduinoIDE() ? 1 : 0,
                    loadedConfig.getProjectMaxLevel(),
                    loadedConfig.getNumberBackups(),
                    loadedConfig.getRegisteredKey(),
                    loadedConfig.getReleaseStream(),
                    loadedConfig.getCurrentTheme()
            );
            loadedConfig.setChanged(false);
        } else {
            logger.log(DEBUG, "Not saving settings this time, changed=" + loadedConfig.isChanged() + ", autoCommit=" + autoCommit);
        }
    }

    @Override
    public ReleaseType getReleaseType() {
        String version = props.getProperty(BUILD_VERSION_KEY, "0.0");
        return VersionInfo.fromString(version).getReleaseType();
    }

    @Override
    public String getVersion() {
        return props.getProperty(BUILD_VERSION_KEY, "0.0");
    }

    @Override
    public String getBuildTimestamp() {
        return props.getProperty(BUILD_TIMESTAMP_KEY, "?");
    }

    @Override
    public VersionInfo getLastRunVersion() {
        return VersionInfo.fromString(loadedConfig.getLastRunVersion());
    }

    @Override
    public void setLastRunVersion(VersionInfo version) {
        loadedConfig.setLastRunVersion(version.toString());
        saveIfNeeded();
    }

    @Override
    public boolean isDefaultRecursiveNamingOn() {
        return loadedConfig.isRecursiveNaming();
    }

    @Override
    public boolean isDefaultSaveToSrcOn() {
        return loadedConfig.isSaveToSrc();
    }

    @Override
    public boolean isDefaultSizedEEPROMStorage() {
        return loadedConfig.isEEPROMSized();
    }

    @Override
    public void setDefaultRecursiveNamingOn(boolean state) {
        loadedConfig.setRecursiveNaming(state);
        saveIfNeeded();
    }

    @Override
    public void setDefaultSaveToSrcOn(boolean state) {
        loadedConfig.setSaveToSrc(state);
        saveIfNeeded();
    }

    @Override
    public void setDefaultSizedEEPROMStorage(boolean state) {
        loadedConfig.setEEPROMSized(state);
        saveIfNeeded();
    }

    @Override
    public Optional<String> getArduinoOverrideDirectory() {
        return Optional.ofNullable(loadedConfig.getArduinoOverrideDirectory());
    }

    @Override
    public Optional<String> getArduinoLibrariesOverrideDirectory() {
        return Optional.ofNullable(loadedConfig.getLibraryOverrideDirectory());
    }

    @Override
    public void setArduinoOverrideDirectory(String overrideDirectory) {
        loadedConfig.setArduinoOverrideDirectory(overrideDirectory);
        saveIfNeeded();
        arduinoChangeListener.arduinoDirectoryHasChanged(
                Optional.ofNullable(overrideDirectory),
                Optional.ofNullable(loadedConfig.getLibraryOverrideDirectory()),
                false);
    }

    @Override
    public void setArduinoLibrariesOverrideDirectory(String overrideDirectory) {
        loadedConfig.setLibraryOverrideDirectory(overrideDirectory);
        saveIfNeeded();
        arduinoChangeListener.arduinoDirectoryHasChanged(
                Optional.ofNullable(loadedConfig.getArduinoOverrideDirectory()),
                Optional.ofNullable(loadedConfig.getLibraryOverrideDirectory()),
                true);
    }

    @Override
    public void setUsingArduinoIDE(boolean libs) {
        loadedConfig.setUsingArduinoIDE(libs);
        saveIfNeeded();
    }

    @Override
    public boolean isUsingArduinoIDE() {
        return loadedConfig.isUsingArduinoIDE();
    }

    @Override
    public void setMenuProjectMaxLevel(int levels) {
        loadedConfig.setProjectMaxLevel(levels);
    }

    @Override
    public int getMenuProjectMaxLevel() {
        return loadedConfig.getProjectMaxLevel();
    }

    @Override
    public Locale getChosenLocale() {
        var localeText = loadedConfig.getLocale();
        if(localeText == null || localeText.equals("DEFAULT")) {
            return Locale.getDefault();
        } else {
            try {
                return Locale.forLanguageTag(localeText);
            } catch(Exception ex) {
                logger.log(ERROR, "Could not load locale so using default", ex);
                return Locale.getDefault();
            }
        }
    }

    @Override
    public void setChosenLocale(Locale locale) {
        if(!locale.equals(Locale.getDefault())) {
            loadedConfig.setLocale(locale.toString());
        } else {
            loadedConfig.setLocale("DEFAULT");
        }
        saveIfNeeded();
    }

    @Override
    public List<String> getAdditionalPluginPaths() {
        logger.log(INFO, "Getting extra plugins");
        DatabaseAppDataStore.ensureTableExists(jdbcTemplate, "TC_MENU_EXTRA_PLUGINS", CREATE_ADD_PLUGIN_SQL);
        return jdbcTemplate.query("SELECT PLUGIN_PATH FROM TC_MENU_EXTRA_PLUGINS ORDER BY PLUGIN_IDX", this::mapPlugin);
    }

    private String mapPlugin(ResultSet resultSet, int i) throws SQLException {
        return resultSet.getString("PLUGIN_PATH");
    }

    @Override
    public void setAdditionalPluginPaths(List<String> path) {
        logger.log(INFO, "Setting extra plugins" + path);

        DatabaseAppDataStore.ensureTableExists(jdbcTemplate, "TC_MENU_EXTRA_PLUGINS", CREATE_ADD_PLUGIN_SQL);
        jdbcTemplate.update("DELETE FROM TC_MENU_EXTRA_PLUGINS");
        int i = 0;
        for(var p : path) {
            jdbcTemplate.update("INSERT INTO TC_MENU_EXTRA_PLUGINS(PLUGIN_IDX,PLUGIN_PATH) values(?,?)", i++, p);
        }
    }

    @Override
    public int getNumBackupItems() {
        return loadedConfig.getNumberBackups();
    }

    @Override
    public void setNumBackupItems(int newNum) {
        loadedConfig.setNumberBackups(newNum);
        saveIfNeeded();
    }

    @Override
    public Optional<String> getLastLoadedProject() {
        return Optional.ofNullable(loadedConfig.getLastLoadedProject());
    }

    @Override
    public void setLastLoadedProject(String absolutePath) {
        loadedConfig.setLastLoadedProject(absolutePath);
        saveIfNeeded();
    }

    @Override
    public void emptyLastLoadedProject() {
        loadedConfig.setLastLoadedProject(null);
        saveIfNeeded();
    }

    @Override
    public void addArduinoDirectoryChangeListener(ArduinoDirectoryChangeListener directoryChangeListener) {
        arduinoChangeListener = directoryChangeListener;
    }

    public void setAutoCommit(boolean autoCommit) {
        if(!this.autoCommit && autoCommit) {
            this.autoCommit = true;
            saveIfNeeded();
        } else {
            this.autoCommit = autoCommit;
        }
    }

    public ReleaseType getReleaseStream() {
        return loadedConfig.getReleaseStream();
    }

    public void setReleaseStream(ReleaseType stream) {
        loadedConfig.setReleaseStream(stream);
        saveIfNeeded();
    }

    public void setCurrentTheme(String theme) {
        loadedConfig.setCurrentTheme(theme);
        saveIfNeeded();
    }

    public String getCurrentTheme() {
        return loadedConfig.getCurrentTheme();
    }

    static class LoadedConfiguration {
        private boolean changed;
        private int projectMaxLevel;
        private int numberBackups;
        private String registeredKey;
        private String lastLoadedProject;
        private String lastRunVersion;
        private String arduinoOverrideDirectory;
        private String libraryOverrideDirectory;
        private String locale;
        private boolean recursiveNaming;
        private boolean saveToSrc;
        private boolean eepromSized;
        private boolean usingArduinoIDE;
        private ReleaseType releaseStream;
        private String currentTheme;

        LoadedConfiguration() {
            changed = false;
            registeredKey = null;
            locale = null;
            lastLoadedProject = null;
            lastRunVersion = "0.0.0";
            projectMaxLevel = 1;
            numberBackups = DEFAULT_NUM_BACKUPS;
            arduinoOverrideDirectory = null;
            libraryOverrideDirectory = null;
            recursiveNaming = false;
            saveToSrc = false;
            eepromSized = true;
            usingArduinoIDE = true;
            releaseStream = ReleaseType.STABLE;
        }

        public boolean isChanged() {
            return changed;
        }

        public void setChanged(boolean changed) {
            this.changed = changed;
        }

        public String getRegisteredKey() {
            return registeredKey;
        }

        public void setRegisteredKey(String registeredKey) {
            this.registeredKey = registeredKey;
            this.changed = true;
        }

        public String getLastLoadedProject() {
            return lastLoadedProject;
        }

        public void setLastLoadedProject(String lastLoadedProject) {
            this.lastLoadedProject = lastLoadedProject;
            this.changed = true;
        }

        public boolean isRecursiveNaming() {
            return recursiveNaming;
        }

        public void setRecursiveNaming(boolean recursiveNaming) {
            this.recursiveNaming = recursiveNaming;
            this.changed = true;
        }

        public boolean isSaveToSrc() {
            return saveToSrc;
        }

        public void setSaveToSrc(boolean saveToSrc) {
            this.saveToSrc = saveToSrc;
            this.changed = true;
        }

        public String getLastRunVersion() {
            return lastRunVersion;
        }

        public void setLastRunVersion(String lastRunVersion) {
            this.lastRunVersion = lastRunVersion;
            this.changed = true;
        }

        public boolean isEEPROMSized() {
            return eepromSized;
        }

        public void setEEPROMSized(boolean eepromSized) {
            this.eepromSized = eepromSized;
            this.changed = true;
        }

        public String getArduinoOverrideDirectory() {
            return arduinoOverrideDirectory;
        }

        public void setArduinoOverrideDirectory(String arduinoOverrideDirectory) {
            this.arduinoOverrideDirectory = arduinoOverrideDirectory;
            changed = true;
        }

        public String getLibraryOverrideDirectory() {
            return libraryOverrideDirectory;
        }

        public void setLibraryOverrideDirectory(String libraryOverrideDirectory) {
            this.libraryOverrideDirectory = libraryOverrideDirectory;
            changed = true;
        }

        public boolean isUsingArduinoIDE() {
            return usingArduinoIDE;
        }

        public void setUsingArduinoIDE(boolean usingArduinoIDE) {
            this.usingArduinoIDE = usingArduinoIDE;
            changed = true;
        }

        public int getProjectMaxLevel() {
            return projectMaxLevel;
        }

        public void setProjectMaxLevel(int projectMaxLevel) {
            this.projectMaxLevel = projectMaxLevel;
            changed = true;
        }

        public int getNumberBackups() {
            return numberBackups;
        }

        public void setNumberBackups(int numberBackups) {
            this.numberBackups = numberBackups;
            changed = true;
        }

        public String getLocale() {
            return locale;
        }

        public void setLocale(String locale) {
            this.locale = locale;
            this.changed = true;
        }

        public ReleaseType getReleaseStream() {
            return releaseStream;
        }

        public void setReleaseStream(ReleaseType releaseStream) {
            this.changed = releaseStream != this.releaseStream;
            this.releaseStream = releaseStream;
        }

        public String getCurrentTheme() {
            return currentTheme;
        }

        public void setCurrentTheme(String currentTheme) {
            this.currentTheme = currentTheme;
            this.changed = true;
        }

        @Override
        public String toString() {
            return "LoadedConfiguration{" +
                    "changed=" + changed +
                    ", projectMaxLevel=" + projectMaxLevel +
                    ", numberBackups=" + numberBackups +
                    ", registeredKey='" + registeredKey + '\'' +
                    ", lastLoadedProject='" + lastLoadedProject + '\'' +
                    ", lastRunVersion='" + lastRunVersion + '\'' +
                    ", arduinoOverrideDirectory='" + arduinoOverrideDirectory + '\'' +
                    ", libraryOverrideDirectory='" + libraryOverrideDirectory + '\'' +
                    ", locale='" + locale + '\'' +
                    ", recursiveNaming=" + recursiveNaming +
                    ", saveToSrc=" + saveToSrc +
                    ", eepromSized=" + eepromSized +
                    ", usingArduinoIDE=" + usingArduinoIDE +
                    ", releaseStream=" + releaseStream +
                    ", currentTheme='" + currentTheme + '\'' +
                    '}';
        }
    }
}
