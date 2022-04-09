package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.scene.Node;

import java.util.concurrent.ScheduledExecutorService;

public class LocalTreeComponentManager extends TreeComponentManager<Node> implements MenuManagerListener {
    private final MenuManagerServer menuMgr;

        public LocalTreeComponentManager(GlobalSettings appSettings, ScheduledExecutorService executor, ThreadMarshaller marshaller,
                                         MenuComponentControl controller, MenuManagerServer menuMgr, ScreenLayoutPersistence layoutPersistence) {
            super(appSettings, executor, marshaller, controller, layoutPersistence);
        this.menuMgr = menuMgr;

        menuMgr.addMenuManagerListener(this);
    }

    @Override
    public void menuItemHasChanged(MenuItem item, boolean remoteChange) {
        if(editorComponents.containsKey(item.getId())) {
            editorComponents.get(item.getId()).onItemUpdated(controller.getMenuTree().getMenuState(item));
        }
    }

    @Override
    public void managerWillStart() {
    }

    @Override
    public void managerWillStop() {

    }
}