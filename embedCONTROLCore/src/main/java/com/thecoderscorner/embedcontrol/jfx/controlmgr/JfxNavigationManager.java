package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.NavigationManager;
import javafx.scene.Node;
import javafx.scene.image.Image;

/**
 * This interface that extends Navigation manager just avoids needing to use generic types to describe the JavaFX
 * version of the interface.
 * @see NavigationManager
 */
public interface JfxNavigationManager extends NavigationManager<Node, Image> {
    /**
     * When set puts any menus that are created into editable mode, so that the settings of each item can be overridden.
     * Caution - calling this will completely reset navigation and reload the ROOT panel.
     * @param editorPresenter the editor presenter
     */
    void setItemEditorPresenter(JfxPanelLayoutEditorPresenter editorPresenter);
}
