/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.remote;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

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

    /**
     * Indicates that an acknowledgment has been received from the embedded device.
     * @param key the correlation ID of the acknowledgement
     * @param item the item it corresponds to (may be null)
     * @param status the status associated with the ack.
     */
    void ackReceived(CorrelationId key, MenuItem item, AckStatus status);

    /**
     * Called when a dialog event occurs on the remote, be it to show or hide a dialog
     * @param mode the mode of the dialog update
     * @param header the text for the header
     * @param buffer the text for the buffer
     * @param btn1 the first button type
     * @param btn2 the second button type
     */
    void dialogUpdate(DialogMode mode, String header, String buffer, MenuButtonType btn1, MenuButtonType btn2);
}
