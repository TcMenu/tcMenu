package com.thecoderscorner.embedcontrol.core.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.thecoderscorner.embedcontrol.core.creators.*;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
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

public abstract class FileConnectionStorage<T extends RemotePanelDisplayable> implements ConnectionStorage<T> {
    protected final System.Logger logger = System.getLogger(FileConnectionStorage.class.getSimpleName());
    protected final PlatformSerialFactory serialFactory;
    protected final JsonMenuItemSerializer serializer;
    protected final GlobalSettings settings;
    protected final ScheduledExecutorService executorService;
    private final Path baseDir;

    public FileConnectionStorage(PlatformSerialFactory serialFactory, JsonMenuItemSerializer serializer,
                                 GlobalSettings settings, ScheduledExecutorService executorService,
                                 Path homeDir) throws IOException {
        this.serialFactory = serialFactory;
        this.serializer = serializer;
        this.settings = settings;
        this.executorService = executorService;

        baseDir = homeDir.resolve("ec_connections");
        if(!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
    }

    public List<T> loadAllRemoteConnections() throws IOException {
        return Files.list(baseDir)
                .filter(p -> p.toString().endsWith("_rc.json"))
                .map(this::processRemotePanel)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<T> processRemotePanel(Path file) {
        try {
            logger.log(INFO, "Attempt load for " + file);

            var content = Files.readString(file);
            var rootElement = JsonParser.parseString(content).getAsJsonObject();
            var panelUuid = UUID.fromString(getJsonStrOrThrow(rootElement, "panelUuid"));
            var creatorNode = getJsonObjOrThrow(rootElement, "creator");
            var creatorType = getJsonStrOrThrow(creatorNode, "type");

            ConnectionCreator creator;

            switch(creatorType) {
                case Rs232ConnectionCreator.MANUAL_RS232_CREATOR_TYPE:
                    creator = new Rs232ConnectionCreator(serialFactory);
                    break;
                case ManualLanConnectionCreator.MANUAL_LAN_JSON_TYPE:
                    creator = new ManualLanConnectionCreator(settings, executorService);
                    break;
                case SimulatorConnectionCreator.SIMULATED_CREATOR_TYPE:
                    creator = new SimulatorConnectionCreator(executorService, serializer);
                    break;
                default:
                    throw new IOException("Unknown type of connection: " + creatorType);
            }

            creator.load(rootElement);

            logger.log(INFO, "Loaded panel UUID='" + panelUuid + "' type='" + creatorType + "' name='" + creator.getName());

            return Optional.of(createPanel(creator, panelUuid));
        }
        catch(Exception ex) {
            logger.log(ERROR, "Panel load failed for " + file, ex);
        }
        return Optional.empty();
    }

    protected abstract T createPanel(ConnectionCreator creator, UUID panelUuid);

    public void savePanel(T panel) {
        var panelFileName = baseDir.resolve(panel.getUuid().toString() + "_rc.json");
        try {
            logger.log(INFO, "Saving panel for " + panel.getPanelName() + " uuid " + panel.getUuid());
            var obj = new JsonObject();
            panel.getCreator().save(obj);
            obj.add("panelUuid", new JsonPrimitive(panel.getUuid().toString()));
            Files.writeString(panelFileName, serializer.getGson().toJson(obj));
        }
        catch (Exception ex) {
            logger.log(ERROR, "Panel save failed for " + panelFileName, ex);
        }
    }
}
