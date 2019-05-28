/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.MenuItem;

/**
 * This interface is implemented when you wish to receive update events from a RemoteMenuController.
 * It gets called back when menu items are changed, the tree is fully populated or if the connectivity
 * state changes. The implementation is then passed to the appropriate instance of RemoteMenuConnector
 * via its addListener method.
 */
public interface RemoteControllerListener {

    /**
     * Called when a menu item has either been added or changed, the valueOnly indicates if the change
     * is just in the latest value, or also in the MenuItem structure too.
     * @param item the item that has changed
     * @param valueOnly true if only the current value has changed, false if the MenuItem has changed too
     */
    void menuItemChanged(MenuItem item, boolean valueOnly);

    /**
     * Indicates that the tree is now fully populated, and therefore all menus that exist on the Arduino
     * also exist locally in the MenuTree.
     */
    void treeFullyPopulated();

    /**
     * Indicates a change in connectivity
     * @param remoteInformation the new connection information
     * @param connected true if connected, otherwise false.
     */
    void connectionState(RemoteInformation remoteInformation, AuthStatus connected);
}
