package com.thecoderscorner.menu.editorui.storage;

import com.thecoderscorner.embedcontrol.core.util.*;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.persist.ReleaseType;
import com.thecoderscorner.menu.persist.VersionInfo;

import java.lang.System.Logger.Level;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

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
    private final System.Logger logger = System.getLogger(JdbcTcMenuConfigurationStore.class.getSimpleName());
    private final LoadedConfiguration loadedConfig;
    private final TccDatabaseUtilities databaseUtilities;
    private boolean autoCommit = true;
    private ArduinoDirectoryChangeListener arduinoChangeListener = null;
    private LinkedList<RecentlyUsedItem> recentItems = new LinkedList<>();

    public JdbcTcMenuConfigurationStore(TccDatabaseUtilities databaseUtilities) {
        this.databaseUtilities = databaseUtilities;
        LoadedConfiguration loaded;
        try(var resourceAsStream = getClass().getResourceAsStream("/version.properties")) {
            props.load( resourceAsStream );
            // if the table doesn't exist, we are probably on first start
            boolean firstStart = !databaseUtilities.checkTableExists("TC_MENU_SETTINGS");
            // once we've determined that, let the database utilities get the DB in the right state
            databaseUtilities.ensureTableFormatCorrect(LoadedConfiguration.class);
            // and if not started before, on the first attempt, we try and copy from preferences or default.
            if(firstStart) createIfNeeded();
            // and lastly load the configuration
            loaded = databaseUtilities.queryPrimaryKey(LoadedConfiguration.class, 0).orElseThrow();

            var r = databaseUtilities.queryStrings("SELECT RECENT_FILE FROM TC_MENU_RECENTS ORDER BY RECENT_IDX");
            recentItems = r.stream().map(rec -> new RecentlyUsedItem(Paths.get(rec).getFileName().toString(), rec))
                    .collect(Collectors.toCollection(LinkedList::new));
            logger.log(INFO, "Loaded all configuration");
        } catch(Exception ex) {
            logger.log(ERROR, "Unable to initialise from database store", ex);
            loaded = new LoadedConfiguration();
        }
        loadedConfig = loaded;
        logger.log(INFO, "Loaded initial designer settings as" + loadedConfig);
    }

    private void createIfNeeded() {
        logger.log(INFO, "First startup on database config.");
        // we are creating a new instance, let us try and copy from preferences.
        boolean nodeExists = false;
        try {
            nodeExists = Preferences.userNodeForPackage(MenuEditorController.class).nodeExists("");
        } catch (BackingStoreException e) {
            nodeExists = false;
        }

        LoadedConfiguration lc = new LoadedConfiguration();
        if(!nodeExists) {
            lc.setUsingArduinoIDE(true);
            lc.setSaveToSrc(false);
            lc.setEEPROMSized(true);
            lc.setRecursiveNaming(false);
            lc.setNumberBackups(DEFAULT_NUM_BACKUPS);
            lc.setLocale("DEFAULT");
            lc.setArduinoOverrideDirectory("");
            lc.setLibraryOverrideDirectory("");
            lc.setProjectMaxLevel(1);
            lc.setCurrentTheme("lightMode");
        } else {
            Preferences prefsNode = Preferences.userNodeForPackage(BaseDialogSupport.class);
            var prefs = new PrefsConfigurationStorage();
            var lastTheme = prefsNode.get("uiTheme", "lightMode");
            logger.log(INFO, "Read preferences from legacy to update");

            saveUniqueRecents(prefs.loadRecents());
            setAdditionalPluginPaths(prefs.getAdditionalPluginPaths());

            lc.setRecursiveNaming(prefs.isDefaultRecursiveNamingOn());
            lc.setLocale(prefs.getChosenLocale().toString());
            lc.setArduinoOverrideDirectory(prefs.getArduinoOverrideDirectory().orElse(null));
            lc.setLibraryOverrideDirectory(prefs.getArduinoLibrariesOverrideDirectory().orElse(null));
            lc.setSaveToSrc(prefs.isDefaultSaveToSrcOn());
            lc.setEEPROMSized(prefs.isDefaultSizedEEPROMStorage());
            lc.setLastLoadedProject(prefs.getLastLoadedProject().orElse(null));
            lc.setLastRunVersion(prefs.getLastRunVersion().toString());
            lc.setUsingArduinoIDE(prefs.isUsingArduinoIDE());
            lc.setProjectMaxLevel(prefs.getMenuProjectMaxLevel());
            lc.setNumberBackups(prefs.getNumBackupItems());
            lc.setRegisteredKey(prefs.getRegisteredKey());
            lc.setReleaseStream(ReleaseType.STABLE);
            lc.setCurrentTheme(lastTheme);
        }

        try {
            databaseUtilities.updateRecord(LoadedConfiguration.class, lc);
            logger.log(INFO, "Initial state of designer settings persisted");
        } catch (DataException e) {
            logger.log(ERROR, "Migrating settings from props failed", e);
        }
    }

    @Override
    public List<String> loadRecents() {
        throw new UnsupportedOperationException();
    }

    public List<RecentlyUsedItem> getRecents() {
        return recentItems;
    }

    @Override
    public void saveUniqueRecents(List<String> recents) {
        try {
            databaseUtilities.ensureTableExists("TC_MENU_RECENTS", CREATE_RECENTS_SQL);
            databaseUtilities.executeRaw("DELETE FROM TC_MENU_RECENTS");
            int i = 0;
            for (var recent : recents) {
                databaseUtilities.executeRaw("INSERT INTO TC_MENU_RECENTS(RECENT_FILE, RECENT_IDX) values(?,?)",
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
            try {
                databaseUtilities.updateRecord(LoadedConfiguration.class, loadedConfig);
            } catch (DataException e) {
                logger.log(ERROR, "Could not save configuration", e);
            }
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
        databaseUtilities.ensureTableExists("TC_MENU_EXTRA_PLUGINS", CREATE_ADD_PLUGIN_SQL);
        try {
            return databaseUtilities.queryStrings("SELECT PLUGIN_PATH FROM TC_MENU_EXTRA_PLUGINS ORDER BY PLUGIN_IDX");
        } catch (DataException e) {
            logger.log(ERROR, "Additional plugins not loaded", e);
            return List.of();
        }
    }

    @Override
    public void setAdditionalPluginPaths(List<String> path) {
        logger.log(INFO, "Setting extra plugins" + path);

        try {
            databaseUtilities.ensureTableExists("TC_MENU_EXTRA_PLUGINS", CREATE_ADD_PLUGIN_SQL);
            databaseUtilities.executeRaw("DELETE FROM TC_MENU_EXTRA_PLUGINS");
            int i = 0;
            for(var p : path) {
                databaseUtilities.executeRaw("INSERT INTO TC_MENU_EXTRA_PLUGINS(PLUGIN_IDX,PLUGIN_PATH) values(?,?)", i++, p);
            }
        } catch (DataException e) {
            logger.log(ERROR, "Could not update plugin paths", e);
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

    public void addToRecents(RecentlyUsedItem recentlyUsedItem) {
        recentItems.addFirst(recentlyUsedItem);

        recentItems = recentItems.stream()
                .filter(recent -> Files.exists(Paths.get(recent.path)))
                .filter(recent -> !recent.name().equals(ConfigurationStorage.RECENT_DEFAULT))
                .distinct()
                .collect(Collectors.toCollection(LinkedList::new));

        saveUniqueRecents(recentItems.stream().map(RecentlyUsedItem::path).toList());
    }

    @TableMapping(tableName = "TC_MENU_SETTINGS", uniqueKeyField = "SETTING_ID")
    public static class LoadedConfiguration {
        private boolean changed;
        @FieldMapping(fieldName = "SETTING_ID", fieldType = FieldType.INTEGER, primaryKey = true)
        private int settingId;
        @FieldMapping(fieldName = "MAX_SCAN_PROJECT_LEVEL", fieldType = FieldType.INTEGER)
        private int projectMaxLevel;
        @FieldMapping(fieldName = "NUM_BACKUPS", fieldType = FieldType.INTEGER)
        private int numberBackups;
        @FieldMapping(fieldName = "REGISTERED_KEY", fieldType = FieldType.VARCHAR)
        private String registeredKey;
        @FieldMapping(fieldName = "LAST_LOADED_PROJECT", fieldType = FieldType.VARCHAR)
        private String lastLoadedProject;
        @FieldMapping(fieldName = "LAST_RUN_VERSION", fieldType = FieldType.VARCHAR)
        private String lastRunVersion;
        @FieldMapping(fieldName = "ARDUINO_OVERRIDE", fieldType = FieldType.VARCHAR)
        private String arduinoOverrideDirectory;
        @FieldMapping(fieldName = "LIBRARIES_OVERRIDE", fieldType = FieldType.VARCHAR)
        private String libraryOverrideDirectory;
        @FieldMapping(fieldName = "CHOSEN_LOCALE", fieldType = FieldType.VARCHAR)
        private String locale;
        @FieldMapping(fieldName = "DEFAULT_RECURSIVE", fieldType = FieldType.BOOLEAN)
        private boolean recursiveNaming;
        @FieldMapping(fieldName = "DEFAULT_SAVE_LOCATION", fieldType = FieldType.BOOLEAN)
        private boolean saveToSrc;
        @FieldMapping(fieldName = "DEFAULT_SIZED_EEPROM", fieldType = FieldType.BOOLEAN)
        private boolean eepromSized;
        @FieldMapping(fieldName = "USING_ARDUINO_IDE", fieldType = FieldType.BOOLEAN)
        private boolean usingArduinoIDE;
        @FieldMapping(fieldName = "RELEASE_STREAM", fieldType = FieldType.ENUM)
        private ReleaseType releaseStream;
        @FieldMapping(fieldName = "CURRENT_THEME", fieldType = FieldType.VARCHAR)
        private String currentTheme;

        public LoadedConfiguration() {
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

    public record RecentlyUsedItem(String name, String path) {
        public String toString() {
            return name;
        }
    }
}
