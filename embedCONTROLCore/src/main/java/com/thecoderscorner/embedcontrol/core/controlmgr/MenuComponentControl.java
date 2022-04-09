package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

public interface MenuComponentControl {
    CorrelationId editorUpdatedItem(MenuItem item, Object menuItem);
    CorrelationId editorUpdatedItemDelta(MenuItem item, int menuItem);
    void connectionStatusChanged(AuthStatus authStatus);
    MenuTree getMenuTree();
    String getConnectionName();
    NavigationManager getNavigationManager();
    void presentIoTAuthPanel();
}
