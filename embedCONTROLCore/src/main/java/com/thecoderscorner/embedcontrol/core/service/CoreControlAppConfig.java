package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CoreControlAppConfig {
    protected final Path tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
    protected final PlatformSerialFactory serialFactory;
    protected final TccDatabaseUtilities databaseUtils;
    private final ApplicationThemeManager themeManager;
    protected DatabaseAppDataStore ecDataStore;
    protected JsonMenuItemSerializer serializer;
    protected GlobalSettings globalSettings;
    protected ScheduledExecutorService executor = Executors.newScheduledThreadPool(4);

    public CoreControlAppConfig() throws Exception {
        var dbHome = tcMenuHome.resolve("h2db");
        if(!Files.exists(tcMenuHome)) {
            try {
                Files.createDirectory(tcMenuHome);
                Files.createDirectory(dbHome);
            } catch (IOException e) {
                System.getLogger("Context").log(System.Logger.Level.ERROR, "Could not create ~/.tcmenu directory");
            }
        }

        var dbSafePath = dbHome.toString().replace('\\', '/') + "/tcmenuConfig";
        Connection c = DriverManager.getConnection("jdbc:hsqldb:file:" + dbSafePath, "SA", "");

        serializer = new JsonMenuItemSerializer();

        databaseUtils = new TccDatabaseUtilities(c);

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
