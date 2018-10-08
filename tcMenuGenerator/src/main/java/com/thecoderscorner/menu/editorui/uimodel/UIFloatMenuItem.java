/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class UIFloatMenuItem extends UIMenuItem<FloatMenuItem> {

    private TextField decimalPlaces;

    public UIFloatMenuItem(FloatMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected FloatMenuItem getChangedMenuItem() {
        FloatMenuItemBuilder builder = FloatMenuItemBuilder.aFloatMenuItemBuilder()
                .withExisting(getMenuItem())
                .withDecimalPlaces(getDecimalPlaces());
        getChangedDefaults(builder);
        return builder.menuItem();
    }

    public int getDecimalPlaces() {
        return safeIntFromProperty(decimalPlaces.textProperty());
    }

    @Override
    protected void internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Decimal Places"), 0, idx);
        decimalPlaces = new TextField(String.valueOf(getMenuItem().getNumDecimalPlaces()));
        decimalPlaces.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(decimalPlaces);
        grid.add(decimalPlaces, 1, idx);
    }
}
