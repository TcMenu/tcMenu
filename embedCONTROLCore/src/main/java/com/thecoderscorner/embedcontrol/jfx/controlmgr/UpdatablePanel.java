package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

/// Represents a `PanelPresentable` that can be updated when menu items change, it is also provided with a
/// tick function so that the implementor can tick down updates that occur. When an item updates the update
/// will be sent through the `itemHasUpdated` method, and you will be on the JavaFx thread when it occurs.
public interface UpdatablePanel {
    /// called every 100 millis by the framework so that you can tick any animations that are in progress.
    void tickAll();

    /// called whenever there is a menu item update so that the display can be updated.
    /// @param item the item that has updated
    void itemHasUpdated(MenuItem item);

    /// called whenever the connection state changes.
    /// @param isUp true if connection up, otherwise false
    void connectionIsUp(boolean isUp);

    /// called whenever an acknowledgement correlation ID is received
    /// @param correlationId the correlation id of the acknowledgement
    /// @param status the status of the acknowledgement
    void acknowledgedCorrelationId(CorrelationId correlationId, AckStatus status);
}
