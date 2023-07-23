package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration

public class CoreControlAppConfig {
    private final Path tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");

    public CoreControlAppConfig() {
        if(!Files.exists(tcMenuHome)) {
            try {
                Files.createDirectory(tcMenuHome);
            } catch (IOException e) {
                System.getLogger("Context").log(System.Logger.Level.ERROR, "Could not create ~/.tcmenu directory");
            }
        }
    }

    @Bean
    public Path databasePath(@Value("root.app.name") String appName) {
        var path = tcMenuHome.resolve(appName).resolve("tcAppStore.db");
        try {
            Files.createDirectory(path);
        } catch(IOException ex) {
            System.getLogger("Context").log(System.Logger.Level.ERROR, "Could not create app directory " + path);
        }
        return path;
    }

    @Bean
    public DataSource dataSource(Path databasePath) {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:" + databasePath);
        return dataSource;
    }

    @Bean
    public JdbcTemplate template(DataSource source) {
        return new JdbcTemplate(source);
    }

    @Bean
    public DatabaseAppDataStore dataStore(JdbcTemplate template, GlobalSettings settings) {
        var dbStore = new DatabaseAppDataStore(template);
        settings.copyFrom(dbStore.getGlobalSettings());
        return dbStore;
    }

    @Bean
    public ScheduledExecutorService executorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Bean
    public PlatformSerialFactory serialFactory(GlobalSettings settings, ScheduledExecutorService executorService) {
        return new Rs232SerialFactory(settings, executorService);
    }

    @Bean
    public GlobalSettings settings() {
        return new GlobalSettings();
    }

    @Bean JsonMenuItemSerializer serializer() {
        return new JsonMenuItemSerializer();
    }
}
