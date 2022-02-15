package com.thecoderscorner.menu.auth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;

import static java.lang.System.Logger.Level.*;

/**
 * Stores authentication to a properties file and then validates against the stored values.
 *
 * NOTE:
 * This is only suited to very simple use cases where the level of security required is not particularly high and
 * the file system of the device is completely secured. The authentication UUIDs are stored PLAIN TEXT.
 */
public class PropertiesAuthenticator implements MenuAuthenticator {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Properties properties = new Properties();
    private final String location;

    public PropertiesAuthenticator(String location) {
        this.location = location;
        try {
            this.properties.load(Files.newBufferedReader(Path.of(location)));
        } catch (IOException e) {
            logger.log(ERROR, "Unable to read authentication properties");
        }
    }

    @Override
    public boolean authenticate(String user, UUID uuid) {
        String val;
        synchronized (this.properties) {
            val = this.properties.getProperty(user);
        }
        if(val == null) return false;
        return UUID.fromString(val).equals(uuid);
    }

    @Override
    public boolean addAuthentication(String user, UUID uuid) {
        try {
            synchronized (properties) {
                properties.setProperty(user, uuid.toString());
                properties.store(Files.newBufferedWriter(Path.of(location)), "TcMenu Auth properties");
            }
            logger.log(INFO, "Wrote auth properties to ", location);
            return true;
        } catch (Exception e) {
            logger.log(ERROR, "Failed to write auth properties to ", location);
            return false;
        }
    }
}
