package com.thecoderscorner.embedcontrol.core.service;

import com.google.gson.JsonParser;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.getJsonObjOrThrow;
import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.getJsonStrOrThrow;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

/**
 * This is the file implementation of connection storage, it saves each item to file using the ScreenLayoutPersistence
 * class. It is mainly a delegate to the layout persistence class that handles all files within a directory.
 * @param <T>
 */
public abstract class FileConnectionStorage<T extends ScreenLayoutPersistence> implements ConnectionStorage<T> {
    protected final System.Logger logger = System.getLogger(FileConnectionStorage.class.getSimpleName());
    protected final PlatformSerialFactory serialFactory;
    protected final GlobalSettings settings;
    protected final ScheduledExecutorService executorService;
    private final Path baseDir;

    public FileConnectionStorage(PlatformSerialFactory serialFactory, JsonMenuItemSerializer serializer,
                                 GlobalSettings settings, ScheduledExecutorService executorService,
                                 Path baseDir) {
        this.serialFactory = serialFactory;
        this.settings = settings;
        this.executorService = executorService;
        this.baseDir = baseDir;
    }

    public List<T> loadAllRemoteConnections() throws IOException {
        return Files.list(baseDir)
                .filter(p -> p.toString().endsWith("-layout.xml"))
                .map(this::createLayoutPersistence)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    protected abstract Optional<T> createLayoutPersistence(Path file);

    public void savePanel(T screenSettings) {
    }

    public boolean deletePanel(UUID uuid) {
        var panelFileName = ScreenLayoutPersistence.uuidToFileName(baseDir, uuid);
        try {
            logger.log(INFO, "Delete panel request for " + uuid + ", file ", panelFileName);
            return Files.deleteIfExists(panelFileName);
        } catch (IOException e) {
            logger.log(ERROR, "Panel File could not be deleted " + uuid);
            return false;
        }
    }
}
