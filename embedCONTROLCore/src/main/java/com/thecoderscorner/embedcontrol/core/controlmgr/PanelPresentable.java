package com.thecoderscorner.embedcontrol.core.controlmgr;

/**
 * Represents a panel that can be displayed onto a UI, it has a name, a UI representation and the possibility to close
 * or remove it. In all cases they will be in a card like layout where panels are pushed onto the layout, and only
 * one can be shown at once, either in a stack, or the panels may be in a selectable list.
 * @param <T> the UI panel type, usually Node
 */
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
