package com.thecoderscorner.embedcontrol.core.controlmgr;

/// Represents a panel that can be displayed onto a UI, it has a name, a UI representation and the possibility to close
/// or remove it. Within Embed Control we use `PanelPresentable` in two ways. Firstly it is used in Embed Control UI
/// for a master-detail interface, where the created panels appear on the left, and upon clicking a panel, it presents
/// on the main area.
///
/// Secondly, it is used as a stack in the menu navigation flow when presenting a connection. In this case, if you
/// present a submenu at a time (non-recursive) then the panels will stack up, and a back button will appear at the top
/// of the connection panel.
///
/// In all cases  will be in a card like layout where panels are pushed onto the layout, and only
/// one can be shown at once, either in a stack, or the panels may be in a selectable list.
///
/// @see com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationHeader
/// @see NavigationManager
/// @param <T> the UI panel type, usually Node
public interface PanelPresentable<T> {
    /**
     * Gets the panel UI component for display, note that this can be called more than once
     * @param width the width of the panel to fit into
     * @return a UI panel
     * @throws Exception if the panel creation fails
     */
    T getPanelToPresent(double width) throws Exception;

    /**
     * @return the title for the panel
     */
    String getPanelName();

    /**
     * @return true if the panel can be removed
     */
    boolean canBeRemoved();

    /**
     * @return true if the panel can be closed
     */
    boolean canClose();

    /**
     * Close the panel and release any resources owned by it
     */
    void closePanel();
}
