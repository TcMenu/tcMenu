package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.customization.MenuItemStore;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuEditorFactory;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.mgr.DialogManager;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteControllerListener;
import com.thecoderscorner.menu.remote.RemoteInformation;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.MenuDialogCommand;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.concurrent.ScheduledExecutorService;

public class RemoteJfxMenuPanelPresentable extends JfxMenuPresentable  {
    private final RemoteControllerListener remoteListener;
    private final RemoteMenuController remoteController;

    public RemoteJfxMenuPanelPresentable(SubMenuItem currentRoot, RemoteMenuController controller, DialogManager dialogViewer,
                                         ScheduledExecutorService executor, ThreadMarshaller marshaller,
                                         MenuComponentControl componentControl, JfxMenuEditorFactory editorFactory,
                                         MenuItemStore store, JfxNavigationManager navigationManager) {
        super(currentRoot, store, navigationManager, executor, marshaller, editorFactory, componentControl);
        remoteController = controller;
        remoteListener = new RemoteControllerListener() {
            @Override
            public void menuItemChanged(MenuItem item, boolean valueOnly) {
                //if (editorComponents.containsKey(item.getId())) {
//                    editorComponents.get(item.getId()).onItemUpdated(controller.getManagedMenu().getMenuState(item));
  //              }
            }

            @Override
            public void treeFullyPopulated() {
            }

            @Override
            public void connectionState(RemoteInformation remoteInformation, AuthStatus connected) {
                //connectionChanged(connected);
                componentControl.connectionStatusChanged(connected);
            }

            @Override
            public void ackReceived(CorrelationId key, MenuItem item, AckStatus status) {
                if(item == null) return; // we ignore dialog acks at the moment.
                //var comp = editorComponents.get(item.getId());
                //if(comp != null) {
                    //comp.onCorrelation(key, status);
                //}
            }

            @Override
            public void dialogUpdate(MenuDialogCommand cmd) {
                marshaller.runOnUiThread(() -> dialogViewer.updateStateFromCommand(cmd));
            }
        };
        remoteController.addListener(remoteListener);

        // handle the case where it's already connected really quick!
        if (controller.getConnector().getAuthenticationStatus() == AuthStatus.CONNECTION_READY) {
            //connectionChanged(AuthStatus.CONNECTION_READY);
        }
    }

    public void dispose() {
        //super.dispose();
        remoteController.removeListener(remoteListener);
    }
}
