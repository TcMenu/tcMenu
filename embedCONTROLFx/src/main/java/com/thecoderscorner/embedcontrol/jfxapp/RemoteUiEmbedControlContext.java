package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.jfxapp.dialog.MainWindowController;
import com.thecoderscorner.embedcontrol.jfxapp.panel.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;

import static java.lang.System.Logger.Level.*;

public class RemoteUiEmbedControlContext implements EmbedControlContext {
    private final System.Logger logger = System.getLogger(RemoteUiEmbedControlContext.class.getSimpleName());
    private ObservableList<PanelPresentable<Node>> allPresentableViews;

    private final VersionHelper versionHelper;
    private final ScheduledExecutorService executorService;
    private final JsonMenuItemSerializer serializer;
    private final PlatformSerialFactory serialFactory;
    private final AppDataStore dataStore;
    private final GlobalSettings settings;
    private MainWindowController controller;

    public RemoteUiEmbedControlContext(ScheduledExecutorService executorService, JsonMenuItemSerializer serializer,
                                       PlatformSerialFactory serialFactory, AppDataStore dataStore,
                                       GlobalSettings settings, VersionHelper helper) {
        this.executorService = executorService;
        this.serializer = serializer;
        this.serialFactory = serialFactory;
        this.dataStore = dataStore;
        this.settings = settings;
        this.versionHelper = helper;
    }

    public void initialize(MainWindowController controller) {
        this.controller = controller;

        var defaultViews = List.of(
                new AboutPanelPresentable(versionHelper),
                new SettingsPanelPresentable(getSettings(), dataStore),
                new NewConnectionPanelPresentable(this),
                new FormManagerPanelPresentable(null, this)
        );

        var loadedLayouts = dataStore.getAllConnections();
        var loadedPanels = loadedLayouts.stream()
                .sorted(Comparator.comparing(TcMenuPersistedConnection::getLastModified).reversed())
                .map(layout ->new RemoteConnectionPanel(this, MenuTree.ROOT, executorService, layout))
                .toList();
        allPresentableViews = FXCollections.observableArrayList();
        allPresentableViews.addAll(defaultViews);
        allPresentableViews.addAll(loadedPanels);

        controller.initialise(getSettings(), allPresentableViews, versionHelper);
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
            allPresentableViews.add(panel);
            controller.panelsChanged(allPresentableViews, Optional.of(panel));
            logger.log(INFO, "Created new panel " + panel.getPanelName());
        } catch (Exception e) {
            logger.log(ERROR, "Panel creation failure", e);
        }
    }

    @Override
    public void deleteConnection(TcMenuPersistedConnection connection) {
        try {
            dataStore.deleteConnection(connection);
            var panel = allPresentableViews.stream()
                    .filter(pp -> pp instanceof RemoteConnectionPanel rcp && rcp.getPersistence().getLocalId() == connection.getLocalId())
                    .findFirst();
            if (panel.isPresent()) {
                allPresentableViews.remove(panel.get());
                controller.panelsChanged(allPresentableViews, Optional.empty());
                logger.log(INFO, "Deleted panel from storage and location " + connection.getName());
            } else {
                logger.log(WARNING, "Request to delete non existing panel from UI " + connection.getName());
            }
        } catch(Exception ex) {
            logger.log(ERROR, "Delete operation failed", ex);
        }
    }

    @Override
    public ConnectionCreator connectionFromDescription(TcMenuPersistedConnection connection) {
        return switch(connection.getConnectionType()) {
            case SIMULATOR -> new SimulatorConnectionCreator(connection.getExtraData(), connection.getName(), executorService, serializer);
            case MANUAL_SOCKET -> new ManualLanConnectionCreator(settings, executorService, connection.getName(), connection.getHostOrSerialId(), Integer.parseInt(connection.getPortOrBaud()));
            case SERIAL_CONNECTION -> new Rs232ConnectionCreator(serialFactory, connection.getHostOrSerialId(), Integer.parseInt(connection.getPortOrBaud()));
        };
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
        try {
            dataStore.updateConnection(newConnection);
            controller.refreshAllPanels();
        } catch (DataException e) {
            logger.log(ERROR, "Update operation failed", e);
        }
    }
}
