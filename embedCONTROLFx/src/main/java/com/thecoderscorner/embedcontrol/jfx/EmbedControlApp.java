/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.dialog.MainWindowController;
import com.thecoderscorner.embedcontrol.jfx.panel.*;
import com.thecoderscorner.embedcontrol.jfx.rs232.Rs232SerialFactory;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.getJsonObjOrThrow;
import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.getJsonStrOrThrow;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class EmbedControlApp extends Application {
    private static final Object metroLock = new Object();
    private static JMetro jMetro = null;
    private static GlobalSettings settings;
    private final ScheduledExecutorService coreExecutor = Executors.newScheduledThreadPool(4);
    private MainWindowController controller;
    private JsonMenuItemSerializer serializer;
    private Rs232SerialFactory serialFactory;
    private Path tcMenuHome;
    private final System.Logger logger = System.getLogger("PanelSerializer");

    @Override
    public void start(Stage primaryStage) throws Exception {
        tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
        if(!Files.exists(tcMenuHome)) Files.createDirectory(tcMenuHome);

        var logName = System.getProperty("devlog") != null ? "dev-logging" : "logging";
        var inputStream = EmbedControlApp.class.getResourceAsStream("/logconf/" + logName + ".properties");
        try
        {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
        catch (final IOException e)
        {
            Logger.getAnonymousLogger().severe("Could not load default logger:" + e.getMessage());
        }

        serializer = new JsonMenuItemSerializer();

        // At this point we build a JavaFX stage and load up our main window
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/img/large_icon.png"))));
        primaryStage.setTitle("embedCONTROL desktop");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        serialFactory = new Rs232SerialFactory(settings, coreExecutor);

        settings = new GlobalSettings();
        settings.load();

        // then we pass the menuTree and remoteControl to the windows controller.
        controller = loader.getController();
        var defaultViews = List.of(
                new AboutPanelPresentable(),
                new SettingsPanelPresentable(settings),
                new NewConnectionPanelPresentable(serialFactory, this::creatorConsumer, settings, coreExecutor, serializer)
        );

        var loadedViews = loadAllRemoteConnections();
        var allViews = new ArrayList<PanelPresentable>();
        allViews.addAll(defaultViews);
        allViews.addAll(loadedViews);

        controller.initialise(settings, allViews);

        // display the main window.
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        getJMetro().setScene(myScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((evt)-> {
            Platform.exit();
            System.exit(0);
        });
    }

    private List<PanelPresentable> loadAllRemoteConnections() throws IOException {
        return Files.list(tcMenuHome)
                .filter(p -> p.toString().endsWith("_rc.json"))
                .map(this::processRemotePanel)
                .filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toList());
    }

    private Optional<PanelPresentable> processRemotePanel(Path file) {
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
                    creator = new ManualLanConnectionCreator(settings, coreExecutor);
                    break;
                case SimulatorConnectionCreator.SIMULATED_CREATOR_TYPE:
                    creator = new SimulatorConnectionCreator(coreExecutor, serializer);
                    break;
                default:
                    throw new IOException("Unknown type of connection: " + creatorType);
            }

            creator.load(rootElement);

            logger.log(INFO, "Loaded panel UUID='" + panelUuid + "' type='" + creatorType + "' name='" + creator.getName());

            var panel = new RemoteConnectionPanel(creator, settings, coreExecutor, panelUuid);
            return Optional.of(panel);
        }
        catch(Exception ex) {
            logger.log(ERROR, "Panel load failed for " + file, ex);
        }
        return Optional.empty();
    }

    private void savePanel(RemoteConnectionPanel panel) {
        var panelFileName = tcMenuHome.resolve(panel.getUuid().toString() + "_rc.json");
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

    private void creatorConsumer(ConnectionCreator connectionCreator) {
        var panel = new RemoteConnectionPanel(connectionCreator, settings, coreExecutor, UUID.randomUUID());
        controller.createdConnection(panel);

        logger.log(INFO, "Created new panel " + panel.getPanelName());

        coreExecutor.execute(() -> savePanel(panel));
    }

    /**
     * Get the JMetro object that we use as the theme.
     * @return the jmetro object.
     */
    public static JMetro getJMetro() {
        synchronized (metroLock) {
            if (jMetro == null) {
                Style style = settings.isDarkMode() ? Style.DARK : Style.LIGHT;
                jMetro = new JMetro(style);
            }
        }
        return jMetro;
    }


}
