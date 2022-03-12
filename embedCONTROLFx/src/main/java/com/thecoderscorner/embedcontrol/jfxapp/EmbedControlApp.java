/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.FileConnectionStorage;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.BaseDialogSupport;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.MainWindowController;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.NewConnectionController;
import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.jfxapp.panel.*;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.*;

public class EmbedControlApp extends Application implements EmbedControlContext {
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
    private ObservableList<PanelPresentable> allPresentableViews;
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
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

        settings = new GlobalSettings();
        settings.load();

        serialFactory = new Rs232SerialFactory(settings, coreExecutor);

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
                new NewConnectionPanelPresentable(settings, this)
        );

        connectionStorage = new FileConnectionStorage<>(serialFactory, serializer, settings, coreExecutor, tcMenuHome) {
            @Override
            protected RemoteConnectionPanel createPanel(ConnectionCreator creator, UUID panelUuid) {
                return new RemoteConnectionPanel(creator, settings, EmbedControlApp.this, panelUuid);
            }
        };

        var loadedViews = connectionStorage.loadAllRemoteConnections();
        allPresentableViews = FXCollections.observableArrayList();
        allPresentableViews.addAll(defaultViews);
        allPresentableViews.addAll(loadedViews);

        controller.initialise(settings, allPresentableViews);
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

    //
    // Context implementation
    //

    @Override
    public ScheduledExecutorService getExecutorService() {
        return coreExecutor;
    }

    @Override
    public JsonMenuItemSerializer getSerializer() {
        return serializer;
    }

    @Override
    public PlatformSerialFactory getSerialFactory() {
        return serialFactory;
    }

    @Override
    public void createConnection(ConnectionCreator connectionCreator) {
        var panel = new RemoteConnectionPanel(connectionCreator, settings, this, UUID.randomUUID());
        controller.createdConnection(panel);

        logger.log(INFO, "Created new panel " + panel.getPanelName());

        coreExecutor.execute(() -> connectionStorage.savePanel(panel));
    }

    @Override
    public void editConnection(UUID identifier) {
        var panel = allPresentableViews.stream()
                .filter(pp -> pp instanceof RemoteConnectionPanel rcp && rcp.getUuid().equals(identifier))
                .findFirst();
        if(panel.isEmpty()) return;
        var connectionPanel = (RemoteConnectionPanel) panel.get();

        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit connection " + connectionPanel.getPanelName());
            dialogStage.initOwner(primaryStage);

            var loader = new FXMLLoader(BaseDialogSupport.class.getResource("/newConnection.fxml"));
            Pane loadedPane = loader.load();
            NewConnectionController editController = loader.getController();
            editController.initialise(settings, this, Optional.of(connectionPanel.getCreator()));

            Scene scene = new Scene(loadedPane);
            getJMetro().setScene(scene);
            dialogStage.setScene(scene);
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();
            var result = editController.getResult();
            result.ifPresent(connectionCreator -> {
                connectionPanel.changeConnectionCreator(connectionCreator);
                connectionStorage.savePanel(connectionPanel);
            });
        } catch (IOException e) {
            logger.log(ERROR, "Failure during connection edit for " + identifier);
        }
    }

    @Override
    public void deleteConnection(UUID identifier) {
        if(connectionStorage.deletePanel(identifier)) {
            var panel = allPresentableViews.stream()
                    .filter(pp -> pp instanceof RemoteConnectionPanel rcp && rcp.getUuid().equals(identifier))
                    .findFirst();
            if(panel.isPresent()) {
                allPresentableViews.remove(panel.get());
                controller.selectPanel(allPresentableViews.get(0));
                logger.log(INFO, "Deleted panel from storage and location " + identifier);
            }
            else {
                logger.log(WARNING, "Request to delete non existing panel from UI " + identifier);
            }
        }
    }
}
