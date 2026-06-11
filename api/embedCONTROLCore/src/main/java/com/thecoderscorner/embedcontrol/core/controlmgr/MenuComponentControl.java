package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

/**
 * Embed Control is used in both local and remote settings, as such there is a need for different implementations of
 * things such as updates, and connection handling. This interface provides a way to provide a suitable implementation
 * for all environments.
 */
public interface MenuComponentControl {
    /**
     * An item has updated from the UI with an absolute update
     * @param item the item that changed
     * @param menuItem the new value
     * @return a correlation or EMPTY_CORRELATION
     */
    CorrelationId editorUpdatedItem(MenuItem item, Object menuItem);

    /**
     * An item has updated from the UI with a delta update
     * @param item the item that changed
     * @param menuItem the new value
     * @return a correlation or EMPTY_CORRELATION
     */
    CorrelationId editorUpdatedItemDelta(MenuItem item, int menuItem);

    /**
     * The connection status change is remote only, and indicates that a change in status has occurred.
     * @param authStatus the new state
     */
    void connectionStatusChanged(AuthStatus authStatus);

    /**
     * @return the menu tree
     */
    MenuTree getMenuTree();

    /**
     * @return the printable connection name
     */
    String getConnectionName();

    /**
     * @return the navigation manager
     */
    NavigationManager getNavigationManager();

    /**
     * Presents an IoT auth panel if it is possible in this configuration.
     */
    void presentIoTAuthPanel();
}
