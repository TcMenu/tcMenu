package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.embedcontrol.core.controlmgr.TreeComponentManager;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
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

        menuMgr.addTreeStructureChangeListener(hint -> {
            // update all non sub menu items as the tree has structurally changed
            var startingPoint = (hint.hasChildren()) ? MenuItemHelper.asSubMenu(hint) : MenuTree.ROOT;
            this.menuMgr.getManagedMenu().getAllMenuItemsFrom(startingPoint).stream()
                    .filter(item -> !(item instanceof SubMenuItem))
                    .filter(item -> editorComponents.containsKey(item.getId()))
                    .forEach(item -> {
                        editorComponents.get(item.getId()).onItemUpdated(controller.getMenuTree().getMenuState(item));
                    });
        });
    }

    @Override
    public void menuItemHasChanged(Object sender, MenuItem item) {
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