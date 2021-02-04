/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.FloatMenuItem;
import com.thecoderscorner.menu.domain.FloatMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIFloatMenuItem extends UIMenuItem<FloatMenuItem> {

    private TextField decimalPlaces;

    public UIFloatMenuItem(FloatMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer);
    }

    @Override
    protected Optional<FloatMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        int dp = safeIntFromProperty(decimalPlaces.textProperty(), "Decimal Places", errors, 1, 6);
        FloatMenuItemBuilder builder = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withExisting(getMenuItem())
                .withDecimalPlaces(dp);
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Decimal Places"), 0, idx);
        decimalPlaces = new TextField(String.valueOf(getMenuItem().getNumDecimalPlaces()));
        decimalPlaces.textProperty().addListener(this::coreValueChanged);
        decimalPlaces.setId("decimalPlacesField");
        TextFormatterUtils.applyIntegerFormatToField(decimalPlaces);
        grid.add(decimalPlaces, 1, idx);
        return idx;
    }
}
