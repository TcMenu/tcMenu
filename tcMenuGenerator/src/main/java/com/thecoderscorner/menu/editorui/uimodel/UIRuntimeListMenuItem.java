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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIRuntimeListMenuItem extends UIMenuItem<RuntimeListMenuItem> {
    private Spinner<Integer> initialRowsSpinner;
    private CheckBox useInfoCheck;

    public UIRuntimeListMenuItem(RuntimeListMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                 BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.LIST_URL);
    }

    @Override
    protected Optional<RuntimeListMenuItem> getChangedMenuItem() {
        RuntimeListMenuItemBuilder builder = RuntimeListMenuItemBuilder.aRuntimeListMenuItemBuilder().withExisting(getMenuItem());
        List<FieldError> errors = new ArrayList<>();
        getChangedDefaults(builder, errors);
        builder.withInitialRows(initialRowsSpinner.getValue());
        builder.withUsingInfoBlock(useInfoCheck.isSelected());
        var item = builder.menuItem();
        return getItemOrReportError(item, errors);
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        idx++;
        initialRowsSpinner = new Spinner<>(0, 255, getMenuItem().getInitialRows());
        initialRowsSpinner.valueProperty().addListener((observable, oldValue, newValue) -> callChangeConsumer());
        pane.add(new Label(bundle.getString("menu.editor.initial.rows")), 0, idx);
        pane.add(initialRowsSpinner, 1, idx);

        idx++;
        useInfoCheck = new CheckBox(bundle.getString("menu.editor.name.from.info"));
        useInfoCheck.setSelected(getMenuItem().isUsingInfoBlock());
        useInfoCheck.setOnAction(e -> callChangeConsumer());
        pane.add(useInfoCheck, 1, idx);

        idx++;
        // nothing to add
        return idx;
    }
}
