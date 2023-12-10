package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CoreControlAppConfig {
    protected final Path tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
    protected final SQLiteDataSource dataSource;
    protected final PlatformSerialFactory serialFactory;
    protected final TccDatabaseUtilities databaseUtils;
    private final ApplicationThemeManager themeManager;
    protected DatabaseAppDataStore ecDataStore;
    protected JsonMenuItemSerializer serializer;
    protected GlobalSettings globalSettings;
    protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    public CoreControlAppConfig() throws Exception {
        if(!Files.exists(tcMenuHome)) {
            try {
                Files.createDirectory(tcMenuHome);
            } catch (IOException e) {
                System.getLogger("Context").log(System.Logger.Level.ERROR, "Could not create ~/.tcmenu directory");
            }
        }

        dataSource = new SQLiteDataSource();
        dataSource.setUrl("jdbc:sqlite:" + tcMenuHome.resolve("tcDataStore.db"));

        serializer = new JsonMenuItemSerializer();

        databaseUtils = new TccDatabaseUtilities(dataSource);

        themeManager = new ApplicationThemeManager();
        globalSettings = new GlobalSettings(themeManager);

        ecDataStore = new DatabaseAppDataStore(databaseUtils);
        ecDataStore.getGlobalSettings(themeManager).ifPresent(ps -> {
            try {
                ps.populateGlobalSettings(globalSettings);
                ps.setColorsToSave(databaseUtils.queryRecords(TcPreferencesColor.class, ""));
            } catch (DataException e) {
                throw new RuntimeException("Unexpected exception during load", e);
            }
        });

        serialFactory = new Rs232SerialFactory(globalSettings, executor);

    }

    public ApplicationThemeManager getThemeManager() {
        return themeManager;
    }

    public Path getHomeDir() {
        return tcMenuHome;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public DatabaseAppDataStore getEcDataStore() {
        return ecDataStore;
    }

    public ScheduledExecutorService getExecutorService() {
        return executor;
    }

    public PlatformSerialFactory getSerialFactory() { return serialFactory; }

    public GlobalSettings getGlobalSettings() {
        return globalSettings;
    }

    JsonMenuItemSerializer getSerializer() {
        return serializer;
    }
}
