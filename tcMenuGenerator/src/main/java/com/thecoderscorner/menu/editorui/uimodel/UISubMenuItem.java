/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.SubMenuItemBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UISubMenuItem extends UIMenuItem<SubMenuItem> {

    private CheckBox secureCheckbox;

    public UISubMenuItem(SubMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer);
    }

    @Override
    protected Optional<SubMenuItem> getChangedMenuItem() {
        SubMenuItemBuilder builder = SubMenuItemBuilder.aSubMenuItemBuilder().withExisting(getMenuItem());
        builder.withSecured(secureCheckbox.isSelected());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        idx++;
        secureCheckbox = new CheckBox("Secure submenu with password");
        secureCheckbox.setSelected(getMenuItem().isSecured());
        pane.add(secureCheckbox, 0, idx, 2, 1);
        return idx;
    }
}
