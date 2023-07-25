package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.MainWindowController;
import com.thecoderscorner.embedcontrol.jfxapp.panel.AboutPanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.panel.NewConnectionPanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.panel.RemoteConnectionPanel;
import com.thecoderscorner.embedcontrol.jfxapp.panel.SettingsPanelPresentable;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.*;

public class RemoteUiEmbedControlContext implements EmbedControlContext {
    private final System.Logger logger = System.getLogger(RemoteUiEmbedControlContext.class.getSimpleName());
    private ObservableList<PanelPresentable<Node>> allPresentableViews;

    private final ScheduledExecutorService executorService;
    private final JsonMenuItemSerializer serializer;
    private final PlatformSerialFactory serialFactory;
    private final AppDataStore dataStore;
    private final GlobalSettings settings;
    private MainWindowController controller;


    public RemoteUiEmbedControlContext(ScheduledExecutorService executorService, JsonMenuItemSerializer serializer,
                                       PlatformSerialFactory serialFactory, AppDataStore dataStore, GlobalSettings settings) {
        this.executorService = executorService;
        this.serializer = serializer;
        this.serialFactory = serialFactory;
        this.dataStore = dataStore;
        this.settings = settings;
    }

    public void initialize(MainWindowController controller) {
        this.controller = controller;

        var defaultViews = List.of(
                new AboutPanelPresentable(),
                new SettingsPanelPresentable(getSettings(), dataStore),
                new NewConnectionPanelPresentable(getSettings(), this)
        );

        var loadedLayouts = dataStore.getAllConnections();
        var loadedPanels = loadedLayouts.stream().map(layout ->
                new RemoteConnectionPanel(this, MenuTree.ROOT, executorService, layout)).toList();
        allPresentableViews = FXCollections.observableArrayList();
        allPresentableViews.addAll(defaultViews);
        allPresentableViews.addAll(loadedPanels);

        controller.initialise(getSettings(), allPresentableViews);
    }


    @Override
    public ScheduledExecutorService getExecutorService() {
        return executorService;
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
    public void createConnection(TcMenuPersistedConnection connectionInfo) {
        try {
            if(connectionInfo.getLocalId() != -1) throw new UnsupportedOperationException("LocalID must be -1 for create");
            var localId = dataStore.updateConnection(connectionInfo);
            connectionInfo = connectionInfo.withNewLocalId(localId);
            var panel = new RemoteConnectionPanel(this, MenuTree.ROOT, executorService, connectionInfo);
            controller.createdConnection(panel);
            logger.log(INFO, "Created new panel " + panel.getPanelName());
        } catch (Exception e) {
            logger.log(ERROR, "Panel creation failure", e);
        }
    }

    @Override
    public void deleteConnection(TcMenuPersistedConnection connection) {
        dataStore.deleteConnection(connection);
        var panel = allPresentableViews.stream()
                .filter(pp -> pp instanceof RemoteConnectionPanel rcp && rcp.getPersistence().getLocalId() == connection.getLocalId())
                .findFirst();
        if (panel.isPresent()) {
            allPresentableViews.remove(panel.get());
            controller.selectPanel(allPresentableViews.get(0));
            logger.log(INFO, "Deleted panel from storage and location " + connection.getName());
        } else {
            logger.log(WARNING, "Request to delete non existing panel from UI " + connection.getName());
        }
    }

    @Override
    public ConnectionCreator connectionFromDescription(TcMenuPersistedConnection connection) {
        return new SimulatorConnectionCreator("", connection.getName(), executorService, serializer);
    }

    @Override
    public GlobalSettings getSettings() {
        return settings;
    }

    @Override
    public AppDataStore getDataStore() {
        return dataStore;
    }

    @Override
    public void updateConnection(TcMenuPersistedConnection newConnection) {
        dataStore.updateConnection(newConnection);
    }
}
