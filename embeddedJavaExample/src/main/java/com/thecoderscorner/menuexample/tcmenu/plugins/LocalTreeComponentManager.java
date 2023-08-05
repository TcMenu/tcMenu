package com.thecoderscorner.menuexample.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxMenuPresentable;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.application.Platform;

public class LocalTreeComponentManager implements MenuManagerListener {
    private final MenuManagerServer menuMgr;
    private final JfxNavigationManager navigationManager;

    public LocalTreeComponentManager(MenuManagerServer menuMgr, JfxNavigationManager navigationManager) {
        this.menuMgr = menuMgr;
        this.navigationManager = navigationManager;

        menuMgr.addMenuManagerListener(this);

        menuMgr.addTreeStructureChangeListener(hint -> {
            // update all non sub menu items as the tree has structurally changed
            menuItemHasChanged(null, null);
        });
    }

    @Override
    public void menuItemHasChanged(Object sender, MenuItem item) {
        Platform.runLater(() -> {
            if (navigationManager.currentNavigationPanel() instanceof JfxMenuPresentable menuPanel) {
                if (item == null) {
                    menuPanel.entirelyRebuildGrid();
                } else {
                    menuPanel.getGridComponent().itemHasUpdated(item);
                }
            }
        });
    }

    @Override
    public void managerWillStart() {
    }

    @Override
    public void managerWillStop() {

    }
}