/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.rs232.Rs232SerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.FileConnectionStorage;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.MainWindowController;
import com.thecoderscorner.embedcontrol.jfxapp.panel.AboutPanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.panel.NewConnectionPanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.panel.RemoteConnectionPanel;
import com.thecoderscorner.embedcontrol.jfxapp.panel.SettingsPanelPresentable;
import com.thecoderscorner.embedcontrol.core.service.CoreControlAppConfig;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.*;

public class EmbedControlApp extends Application implements EmbedControlContext {
    private MainWindowController controller;
    private final System.Logger logger = System.getLogger("PanelSerializer");
    private ObservableList<PanelPresentable<Node>> allPresentableViews;
    private Stage primaryStage;
    private ApplicationContext applicationContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        startUpLogging();

        applicationContext = new AnnotationConfigApplicationContext(CoreControlAppConfig.class);

        // At this point we build a JavaFX stage and load up our main window
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/fximg/large_icon.png"))));
        primaryStage.setTitle("embedCONTROL desktop");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        // then we pass the menuTree and remoteControl to the Windows controller.
        controller = loader.getController();
        setupMainDisplayablePanels();

        // display the main window.
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((evt)-> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void setupMainDisplayablePanels() throws IOException {
        var defaultViews = List.of(
                new AboutPanelPresentable(),
                new SettingsPanelPresentable(getSettings(), applicationContext.getBean(AppDataStore.class)),
                new NewConnectionPanelPresentable(getSettings(), this, Optional.empty())
        );

        /*connectionStorage = new RemoteAppFileConnectionStorage(serialFactory, serializer, settings, coreExecutor, appDataDir);

        var loadedLayouts = connectionStorage.loadAllRemoteConnections();
        var loadedPanels = loadedLayouts.stream().map(layout ->
                new RemoteConnectionPanel(getSettings(), this, MenuTree.ROOT)).toList();
        allPresentableViews = FXCollections.observableArrayList();
        allPresentableViews.addAll(defaultViews);
        allPresentableViews.addAll(loadedPanels);*/

        controller.initialise(getSettings(), allPresentableViews);
    }

    private GlobalSettings getSettings() {
        return applicationContext.getBean(GlobalSettings.class);
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

    //
    // Context implementation
    //

    @Override
    public ScheduledExecutorService getExecutorService() {
        return applicationContext.getBean(ScheduledExecutorService.class);
    }

    @Override
    public JsonMenuItemSerializer getSerializer() {
        return applicationContext.getBean(JsonMenuItemSerializer.class);
    }

    @Override
    public PlatformSerialFactory getSerialFactory() {
        return applicationContext.getBean(PlatformSerialFactory.class);
    }

    @Override
    public void createConnection(ConnectionCreator connectionCreator) {
        /*var layoutPersistence = new RemoteAppScreenLayoutPersistence(new MenuTree(), settings, UUID.randomUUID(), appDataDir,
                16, serialFactory, coreExecutor, connectionCreator);
        try {
            var panel = new RemoteConnectionPanel(settings, this, MenuTree.ROOT);
            controller.createdConnection(panel);
            logger.log(INFO, "Created new panel " + panel.getPanelName());
        } catch (Exception e) {
            logger.log(ERROR, "Panel creation failure", e);
        }*/
    }

    @Override
    public void deleteConnection(UUID identifier) {
        /*if(connectionStorage.deletePanel(identifier)) {
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
        }*/
    }

    private class RemoteAppFileConnectionStorage extends FileConnectionStorage<RemoteAppScreenLayoutPersistence> {
        public RemoteAppFileConnectionStorage(Rs232SerialFactory serialFactory, JsonMenuItemSerializer serializer, GlobalSettings settings, ScheduledExecutorService coreExecutor, Path appDataDir) {
            super(serialFactory, serializer, settings, coreExecutor, appDataDir);
        }

        @Override
        protected Optional<RemoteAppScreenLayoutPersistence> createLayoutPersistence(Path file) {
            var uuid = UUID.fromString(file.getFileName().toString().replace("-layout.xml", ""));
            /*var layout = new RemoteAppScreenLayoutPersistence(new MenuTree(), settings, uuid, appDataDir,
                    16, serialFactory, coreExecutor);
            layout.loadApplicationData();*/
            //return Optional.of(layout);
            return Optional.empty();
        }

    }
}
