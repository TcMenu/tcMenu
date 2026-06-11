package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

/// This interface represents an item that can be drawn onto a display, it does not say what the control should be
/// directly, the control is created by a call to createComponent, which generates the required UI node.
///
/// In order to present a menu item onto the display, we need to work out what text needs to be displayed, what kind
/// of control is capable of presenting the menu item, and handling updates including a momentary change in color on
/// value change. Error handling and correlation is also dealt with here.
///
/// You normally create items of this class by interacting with the `MenuEditorFactory` interface. If you're trying
/// to create a custom page within `EmbedControl` it is recommended to start with `BaseCustomMenuPanel` as that already
/// creates everything you're likely to need, and has examples of creating controls.
///
/// @see BaseEditorComponent
/// @param <T> the base node type for the UI
public interface EditorComponent<T> {

    /**
     * The possible alignments for the items within the control
     */
    enum PortableAlignment { LEFT, RIGHT, CENTER, LEFT_VAL_RIGHT }

    /**
     * The rendering statuses that the control can be in, to indicate in progress, recent updates or even an error
     */
    enum RenderingStatus { NORMAL, RECENT_UPDATE, EDIT_IN_PROGRESS, CORRELATION_ERROR }

    /**
     * The item has been updated to a new value and that value needs to be presented
     * @param newValue the menu state containing the update
     */
    void onItemUpdated(MenuItem item, MenuState<?> newValue);

    /**
     * There has been a structural change that requires the item to be recalculated.
     * @param item the replacement item after a structural update.
     */
    void structuralChange(MenuItem item);

    /**
     * A correlation has been received from the remote and needs processing
     * @param correlationId the correlation ID, possibly EMPTY_CORRELATION
     * @param status the acknowledgement status
     */
    void onCorrelation(CorrelationId correlationId, AckStatus status);

    /**
     * Should be called frequently to allow the momentary rendering statuses to be cleared, usually called 1/10 sec
     * by the TreeComponentManager in most systems. Do not do anything here that locks or takes time. It is not called
     * on the UI thread.
     */
    void tick();

    /**
     * Create the underlying UI component that represents this menu item. This should be called once per item grid
     * population only
     * @return the UI node
     */
    T createComponent();
}
