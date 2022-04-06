package com.thecoderscorner.menu.auth;

import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.mgr.DialogViewer;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static java.nio.file.StandardOpenOption.*;

/**
 * Stores authentication to a properties file and then validates against the stored values. By default, there are no
 * authentication pairs stored, and the secure passcode is "1234"
 *
 * NOTE:
 * This is only suited to very simple use cases where the level of security required is not particularly high and
 * the file system of the device is completely secured. The authentication UUIDs are stored PLAIN TEXT.
 */
public class PropertiesAuthenticator implements MenuAuthenticator {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final Properties properties = new Properties();
    private final String location;
    private DialogManager dialogManager;

    public PropertiesAuthenticator(String location) {
        this(location, null);
    }

    public PropertiesAuthenticator(String location, DialogManager dialogManager) {
        this.location = location;
        this.dialogManager = dialogManager;
        try {
            this.properties.load(Files.newBufferedReader(Path.of(location)));
        } catch (IOException e) {
            logger.log(ERROR, "Unable to read authentication properties");
        }
    }

    public void setDialogManager(DialogManager dialogManager) {
        this.dialogManager = dialogManager;
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

    /**
     * Adds an authentication token to the store, it assumes that all appropriate permission from the user has
     * been sought.
     * @param user the user to add
     * @param uuid the uuid associated with the user
     * @return a future that can be tracked to indicate if the authentication was accepted
     */
    @Override
    public CompletableFuture<Boolean> addAuthentication(String user, UUID uuid) {
        if(dialogManager == null) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                logger.log(INFO, "Request for authentication with " + user);
                var shouldProceed = new AtomicBoolean(false);
                var dialogLatch = new CountDownLatch(1);
                dialogManager.withTitle("Pair with " + user, true)
                        .withMessage("Be sure you know where this connection originated", true)
                        .withDelegate(DialogViewer.DialogShowMode.LOCAL_DELEGATE_LOCKED, menuButtonType -> {
                            shouldProceed.set(menuButtonType == MenuButtonType.ACCEPT);
                            dialogLatch.countDown();
                            return true;
                        })
                        .showDialogWithButtons(MenuButtonType.ACCEPT, MenuButtonType.CANCEL);
                if(!dialogLatch.await(30, TimeUnit.SECONDS)) {
                    logger.log(INFO, "Dialog Latch timed out without user operation");
                }
                if(shouldProceed.get()) {
                    synchronized (properties) {
                        Path pathLocation = Path.of(location);
                        properties.setProperty(user, uuid.toString());
                        properties.store(Files.newBufferedWriter(pathLocation, CREATE, TRUNCATE_EXISTING), "TcMenu Auth properties");
                    }
                    logger.log(INFO, "Wrote auth properties to ", location);
                    return true;
                }
                else {
                    logger.log(INFO, "Pairing dialog was not accepted");
                    return false;
                }
            } catch (Exception e) {
                logger.log(ERROR, "Failed to write auth properties to ", location);
                return false;
            }
        });
    }

    @Override
    public boolean doesPasscodeMatch(String passcode) {
        return properties.getProperty("securityPasscode", "1234").equals(passcode);
    }
}
