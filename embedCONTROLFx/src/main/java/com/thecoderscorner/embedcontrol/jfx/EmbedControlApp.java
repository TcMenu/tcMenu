/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.FileConnectionStorage;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.LogManager;
import java.util.logging.Logger;

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
    private FileConnectionStorage<RemoteConnectionPanel> connectionStorage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
        if(!Files.exists(tcMenuHome)) Files.createDirectory(tcMenuHome);

        startUpLogging();

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

        // then we pass the menuTree and remoteControl to the Windows controller.
        controller = loader.getController();
        setupMainDisplayablePanels();

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

    private void setupMainDisplayablePanels() throws IOException {
        var defaultViews = List.of(
                new AboutPanelPresentable(),
                new SettingsPanelPresentable(settings),
                new NewConnectionPanelPresentable(serialFactory, this::creatorConsumer, settings, coreExecutor, serializer)
        );

        connectionStorage = new FileConnectionStorage<>(serialFactory, serializer, settings, coreExecutor, tcMenuHome) {
            @Override
            protected RemoteConnectionPanel createPanel(ConnectionCreator creator, UUID panelUuid) {
                return new RemoteConnectionPanel(creator, settings, executorService, panelUuid);
            }
        };

        var loadedViews = connectionStorage.loadAllRemoteConnections();
        var allViews = new ArrayList<PanelPresentable>();
        allViews.addAll(defaultViews);
        allViews.addAll(loadedViews);

        controller.initialise(settings, allViews);
    }

    private void startUpLogging() {
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
    }

    private void creatorConsumer(ConnectionCreator connectionCreator) {
        var panel = new RemoteConnectionPanel(connectionCreator, settings, coreExecutor, UUID.randomUUID());
        controller.createdConnection(panel);

        logger.log(INFO, "Created new panel " + panel.getPanelName());

        coreExecutor.execute(() -> connectionStorage.savePanel(panel));
    }

    /**
     * Get the JMetro object that we use as the theme.
     * @return the JMetro object.
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
