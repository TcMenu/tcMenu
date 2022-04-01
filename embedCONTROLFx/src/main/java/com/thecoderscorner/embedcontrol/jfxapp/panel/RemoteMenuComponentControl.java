package com.thecoderscorner.embedcontrol.jfxapp.panel;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.NavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class RemoteMenuComponentControl implements MenuComponentControl {
    private final RemoteMenuController controller;
    private final NavigationManager navigationManager;
    private final AtomicReference<Consumer<AuthStatus>> authStatusChangeConsumer = new AtomicReference<>();

    RemoteMenuComponentControl(RemoteMenuController controller, NavigationManager navMgr) {
        this.controller = controller;
        this.navigationManager = navMgr;
    }

    public void setAuthStatusChangeConsumer(Consumer<AuthStatus> consumer) {
        authStatusChangeConsumer.set(consumer);
    }

    @Override
        public CorrelationId editorUpdatedItem(MenuItem item, Object val) {
            return controller.sendAbsoluteUpdate(item, val);
        }

        @Override
        public CorrelationId editorUpdatedItemDelta(MenuItem item, int delta) {
            return controller.sendDeltaUpdate(item, delta);
        }

        @Override
        public void connectionStatusChanged(AuthStatus authStatus) {
            var consumer = authStatusChangeConsumer.get();
            if(consumer != null) consumer.accept(authStatus);
        }

        @Override
        public MenuTree getMenuTree() {
            return controller.getManagedMenu();
        }

        @Override
        public String getConnectionName() {
            var rp = controller.getConnector().getRemoteParty();
            if(rp != null) {
                return rp.getName() + " - " + rp.getPlatform().getDescription() + " V" + rp.getMajorVersion() + "." + rp.getMinorVersion();
            }
            else return controller.getConnector().getConnectionName();
        }

        @Override
        public NavigationManager getNavigationManager() {
            return navigationManager;
        }
    }
