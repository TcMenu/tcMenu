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

public class UITextMenuItem extends UIMenuItem<TextMenuItem> {

    private TextField lenField;

    public UITextMenuItem(TextMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected TextMenuItem getChangedMenuItem() {
        TextMenuItemBuilder builder = TextMenuItemBuilder.aTextMenuItemBuilder()
                .withExisting(getMenuItem())
                .withLength(getMaxLength());
        getChangedDefaults(builder);
        return builder.menuItem();
    }

    public int getMaxLength() {
        return safeIntFromProperty(lenField.textProperty());
    }

    @Override
    protected void internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Max. length"), 0, idx);
        lenField = new TextField(String.valueOf(getMenuItem().getTextLength()));
        lenField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(lenField);
        grid.add(lenField, 1, idx);
    }
}
