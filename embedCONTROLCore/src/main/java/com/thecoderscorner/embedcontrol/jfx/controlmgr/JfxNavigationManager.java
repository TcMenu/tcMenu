package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.NavigationManager;
import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.menu.domain.MenuItem;
import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 * This interface that extends Navigation manager for JavaFX based UIs, it has a couple of extra helper methods for
 * allowing layout edit mode and also to allow for custom navigation on the UI.
 * @see NavigationManager
 */
public interface JfxNavigationManager extends NavigationManager<Node, Image> {
    /**
     * When set puts any menus that are created into editable mode, so that the settings of each item can be overridden.
     * Caution - calling this will completely reset navigation and reload the ROOT panel.
     * @param editorPresenter the editor presenter
     */
    void setItemEditorPresenter(JfxPanelLayoutEditorPresenter editorPresenter);

    /**
     * Allows the overriding of display panels on a per sub menu basis, for example you could provide custom UIs for
     * a few panels and use the standard UIs for all the others. You provide a {@link PanelPresentable} implementation
     * that will be called upon to be displayed when the submenu is on display. Associated UIs will be lazy created
     * as needed.
     *
     * @param theItem the sub menu item that has a custom panel
     * @param toPresent the panel to be present when the submenu is selected.
     */
    void addCustomMenuPanel(MenuItem theItem, PanelPresentable<Node> toPresent);
}
