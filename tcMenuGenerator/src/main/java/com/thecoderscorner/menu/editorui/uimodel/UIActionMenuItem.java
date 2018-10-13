/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class UIActionMenuItem extends UIMenuItem<ActionMenuItem> {

    public UIActionMenuItem(ActionMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected ActionMenuItem getChangedMenuItem() {
        ActionMenuItemBuilder builder = ActionMenuItemBuilder.anActionMenuItemBuilder().withExisting(getMenuItem());
        getChangedDefaults(builder);
        return builder.menuItem();
    }

    @Override
    protected void internalInitPanel(GridPane pane, int idx) {
        // nothing to add
    }
}
