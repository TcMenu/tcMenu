/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.ActionMenuItem;
import com.thecoderscorner.menu.domain.ActionMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIActionMenuItem extends UIMenuItem<ActionMenuItem> {

    public UIActionMenuItem(ActionMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected Optional<ActionMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        ActionMenuItemBuilder builder = ActionMenuItemBuilder.anActionMenuItemBuilder().withExisting(getMenuItem());
        getChangedDefaults(builder, errors);

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected void internalInitPanel(GridPane pane, int idx) {
        // nothing to add
    }
}
