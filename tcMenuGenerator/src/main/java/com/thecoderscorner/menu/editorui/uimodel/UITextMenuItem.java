/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.EditableTextMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UITextMenuItem extends UIMenuItem<EditableTextMenuItem> {

    private TextField lenField;
    private ComboBox<EditItemType> editTypeField;

    public UITextMenuItem(EditableTextMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                          BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.TEXT_URL);
    }

    @Override
    protected Optional<EditableTextMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        EditableTextMenuItemBuilder builder = EditableTextMenuItemBuilder.aTextMenuItemBuilder()
                .withExisting(getMenuItem())
                .withLength(safeIntFromProperty(lenField.textProperty(), "MaxLength", errors, 1, 256))
                .withEditItemType(editTypeField.getValue());
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;

        grid.add(new Label("Max. length"), 0, idx);
        lenField = new TextField(String.valueOf(getMenuItem().getTextLength()));
        lenField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(lenField);
        grid.add(lenField, 1, idx);

        idx++;
        grid.add(new Label("Editor Type"), 0, idx);
        editTypeField = new ComboBox<>(FXCollections.observableArrayList(EditItemType.values()));
        editTypeField.getSelectionModel().select(getMenuItem().getItemType());
        editTypeField.valueProperty().addListener((observable, oldValue, newValue) -> callChangeConsumer());
        grid.add(editTypeField, 1, idx);
        return idx;
    }
}
