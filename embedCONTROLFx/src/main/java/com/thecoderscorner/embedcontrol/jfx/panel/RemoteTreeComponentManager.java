package com.thecoderscorner.embedcontrol.jfx.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.scene.Node;

import java.util.concurrent.ScheduledExecutorService;

public class RemoteTreeComponentManager extends TreeComponentManager<Node> {
    private final RemoteControllerListener remoteListener;
    private final RemoteMenuController remoteController;

    public RemoteTreeComponentManager(ScreenManager<Node> screenManager, RemoteMenuController controller,
                                      GlobalSettings appSettings, DialogViewer dialogViewer,
                                      ScheduledExecutorService executor, ThreadMarshaller marshaller,
                                      MenuComponentControl componentControl) {
        super(screenManager, appSettings, dialogViewer, executor, marshaller, componentControl);
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
                marshaller.runOnUiThread(() -> {
                    reset();
                    screenManager.clear();
                    editorComponents.clear();
                    renderMenuRecursive(MenuTree.ROOT, true);
                });
            }

            @Override
            public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
                connectionChanged(connected);
                dialogViewer.statusHasChanged(connected);
            }

            @Override
            public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                for (var uiItem : editorComponents.values()) {
                    uiItem.onCorrelation(key, status);
                }
            }

            @Override
            public void dialogUpdate(DialogMode mode, String header, String buffer, MenuButtonType b1, MenuButtonType b2) {
                marshaller.runOnUiThread(() -> {
                    dialogViewer.show(mode == DialogMode.SHOW);
                    if (mode == DialogMode.SHOW) {
                        dialogViewer.setButton1(b1);
                        dialogViewer.setButton2(b2);
                        dialogViewer.setText(header, buffer);
                    }
                });
            }
        };
        controller.addListener(remoteListener);

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
