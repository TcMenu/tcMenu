package PACKAGE_NAME_REPLACEMENT.tcmenu.plugins;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.mgr.MenuManagerListener;
import com.thecoderscorner.menu.mgr.MenuManagerServer;
import javafx.scene.Node;

import java.util.concurrent.ScheduledExecutorService;

public class LocalTreeComponentManager extends TreeComponentManager<Node> implements MenuManagerListener {
    private MenuManagerServer menuMgr;

    public LocalTreeComponentManager(ScreenManager<Node> screenManager, GlobalSettings appSettings, DialogViewer dialogViewer,
                                     ScheduledExecutorService executor, ThreadMarshaller marshaller,
                                     MenuComponentControl controller, MenuManagerServer menuMgr) {
        super(screenManager, appSettings, dialogViewer, executor, marshaller, controller);
        this.menuMgr = menuMgr;

        menuMgr.addMenuManagerListener(this);
    }

    public void presentSubMenu(SubMenuItem root, boolean recurse) {
        renderMenuRecursive(root, recurse);
    }

    @Override
    public void menuItemHasChanged(MenuItem item, boolean remoteChange) {
        if(editorComponents.containsKey(item.getId())) {
            editorComponents.get(item.getId()).onItemUpdated(controller.getMenuTree().getMenuState(item));
        }
    }

    @Override
    public void managerWillStart() {
        presentSubMenu(MenuTree.ROOT, false);
    }

    @Override
    public void managerWillStop() {

    }
}