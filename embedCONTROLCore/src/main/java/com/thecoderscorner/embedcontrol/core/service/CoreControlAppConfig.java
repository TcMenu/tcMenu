package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class CoreControlAppConfig {
    protected final Path tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
    protected final SQLiteDataSource dataSource;
    protected final PlatformSerialFactory serialFactory;
    protected final TccDatabaseUtilities databaseUtils;
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

        globalSettings = new GlobalSettings();

        serializer = new JsonMenuItemSerializer();

        databaseUtils = new TccDatabaseUtilities(dataSource);

        ecDataStore = new DatabaseAppDataStore(databaseUtils);
        ecDataStore.getGlobalSettings().ifPresent(ps -> ps.populateGlobalSettings(globalSettings));

        serialFactory = new Rs232SerialFactory(globalSettings, executor);

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
