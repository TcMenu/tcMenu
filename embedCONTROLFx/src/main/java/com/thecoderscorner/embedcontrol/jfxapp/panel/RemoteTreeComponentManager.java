package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.DialogViewer;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;

import java.util.concurrent.ScheduledExecutorService;

public class RemoteTreeComponentManager extends TreeComponentManager<Node> {
    private final RemoteControllerListener remoteListener;
    private final RemoteMenuController remoteController;

    public RemoteTreeComponentManager(RemoteMenuController controller,
                                      GlobalSettings appSettings, DialogViewer dialogViewer,
                                      ScheduledExecutorService executor, ThreadMarshaller marshaller,
                                      MenuComponentControl componentControl, ScreenLayoutPersistence layoutPersistence) {
        super(appSettings, executor, marshaller, componentControl, layoutPersistence);
        remoteController = controller;
        remoteListener = new RemoteControllerListener() {
            @Override
            public void menuItemChanged(MenuItem item, boolean valueOnly) {
                if (editorComponents.containsKey(item.getId())) {
                    editorComponents.get(item.getId()).onItemUpdated(controller.getManagedMenu().getMenuState(item));
                }
            }

            @Override
            public void treeFullyPopulated() {
            }

            @Override
            public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
                connectionChanged(connected);
                componentControl.connectionStatusChanged(connected);
            }

            @Override
            public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                var comp = editorComponents.get(item.getId());
                if(comp != null) {
                    comp.onCorrelation(key, status);
                }
            }

            @Override
            public void dialogUpdate(MenuDialogCommand cmd) {
                marshaller.runOnUiThread(() -> dialogViewer.updateStateFromCommand(cmd));
            }
        };
        remoteController.addListener(remoteListener);

        // handle the case where it's already connected really quick!
        if (controller.getConnector().getAuthenticationStatus() == AuthStatus.CONNECTION_READY) {
            connectionChanged(AuthStatus.CONNECTION_READY);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        remoteController.removeListener(remoteListener);
    }
}
