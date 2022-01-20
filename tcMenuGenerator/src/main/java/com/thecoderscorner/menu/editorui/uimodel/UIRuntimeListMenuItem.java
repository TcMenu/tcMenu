/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItem;
import com.thecoderscorner.menu.domain.RuntimeListMenuItemBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIRuntimeListMenuItem extends UIMenuItem<RuntimeListMenuItem> {

    public UIRuntimeListMenuItem(RuntimeListMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                 BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.LIST_URL);
    }

    @Override
    protected Optional<RuntimeListMenuItem> getChangedMenuItem() {
        RuntimeListMenuItemBuilder builder = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder().withExisting(getMenuItem());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);
        RuntimeListMenuItem item = builder.menuItem();
        functionNameTextField.setText(item.getFunctionName());
        return getItemOrReportError(item, errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        functionNameTextField.setDisable(true);
        // nothing to add
        return idx;
    }
}
