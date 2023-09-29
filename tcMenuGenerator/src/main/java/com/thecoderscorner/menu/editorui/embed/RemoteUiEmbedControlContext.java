package com.thecoderscorner.menu.editorui.embed;

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
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.persist.JsonMenuItemSerializer;
import javafx.collections.ObservableList;
import javafx.scene.Node;

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

    public RemoteUiEmbedControlContext(ScheduledExecutorService executorService, JsonMenuItemSerializer serializer,
                                       PlatformSerialFactory serialFactory, AppDataStore dataStore,
                                       GlobalSettings settings) {
        this.executorService = executorService;
        this.serializer = serializer;
        this.serialFactory = serialFactory;
        this.dataStore = dataStore;
        this.settings = settings;
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
    public void deleteConnection(TcMenuPersistedConnection connection) {
        try {
            dataStore.deleteConnection(connection);
            var panel = allPresentableViews.stream()
                    .filter(pp -> pp instanceof RemoteConnectionPanel rcp && rcp.getPersistence().getLocalId() == connection.getLocalId())
                    .findFirst();
            if (panel.isPresent()) {
                allPresentableViews.remove(panel.get());
                MenuEditorApp.getInstance().embedControlRefresh();
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
            MenuEditorApp.getInstance().embedControlRefresh();
        } catch (DataException e) {
            logger.log(ERROR, "Update operation failed", e);
        }
    }
}
