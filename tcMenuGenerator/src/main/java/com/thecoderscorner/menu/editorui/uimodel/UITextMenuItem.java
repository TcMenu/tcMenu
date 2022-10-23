/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EditItemType;
import com.thecoderscorner.menu.domain.EditableTextMenuItem;
import com.thecoderscorner.menu.domain.EditableTextMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
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
import java.util.regex.Pattern;

import static com.thecoderscorner.menu.editorui.uimodel.TextFormatterUtils.applyFormatToField;
import static com.thecoderscorner.menu.editorui.uimodel.TextFormatterUtils.applyIntegerFormatToField;

public class UITextMenuItem extends UIMenuItem<EditableTextMenuItem> {

    private TextField lenField;
    private TextField defaultValueField;
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

        String text = defaultValueField.getText();
        if(!verifyValueForEditType(editTypeField.getValue())) {
            errors.add(new FieldError("Value is not valid " + text, "defaultValue"));
        } else {
            MenuItemHelper.setMenuState(getMenuItem(), text, menuTree);
        }

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;

        grid.add(new Label("Max. length"), 0, idx);
        lenField = new TextField(String.valueOf(getMenuItem().getTextLength()));
        lenField.textProperty().addListener(this::coreValueChanged);
        lenField.setId("textLength");
        applyIntegerFormatToField(lenField);
        grid.add(lenField, 1, idx);

        idx++;
        grid.add(new Label("Editor Type"), 0, idx);
        editTypeField = new ComboBox<>(FXCollections.observableArrayList(EditItemType.values()));
        editTypeField.getSelectionModel().select(getMenuItem().getItemType());
        editTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
            restrictValueForEditType(newValue);
            callChangeConsumer();
        });
        editTypeField.setId("textEditType");
        grid.add(editTypeField, 1, idx);

        idx++;
        grid.add(new Label("Default Value"), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, "");
        defaultValueField = new TextField(value);
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        restrictValueForEditType(getMenuItem().getItemType());
        grid.add(defaultValueField, 1, idx);

        return idx;
    }

    private void restrictValueForEditType(EditItemType itemType) {
        switch (itemType) {

            case PLAIN_TEXT -> {
                defaultValueField.setPromptText("any text");
                applyFormatToField(defaultValueField, Pattern.compile("^[^\"]*$"));
            }
            case IP_ADDRESS -> {
                defaultValueField.setPromptText("nnn.nnn.nnn.nnn");
                applyFormatToField(defaultValueField, Pattern.compile("[\\d.]*$"));
            }
            case TIME_24H, TIME_12H, TIME_24_HUNDREDS, TIME_DURATION_SECONDS, TIME_DURATION_HUNDREDS, TIME_24H_HHMM, TIME_12H_HHMM -> {
                defaultValueField.setPromptText("HH:mm:ss:ff (24hr)");
                applyFormatToField(defaultValueField, Pattern.compile("^[\\d.:/]*$"));
            }
            case GREGORIAN_DATE -> {
                defaultValueField.setPromptText("yyyy/mm/dd");
                applyFormatToField(defaultValueField, Pattern.compile("^[\\d/]*$"));
            }
        }
    }

    private boolean verifyValueForEditType(EditItemType itemType) {
        if(defaultValueField.getText().isEmpty()) return true;

        var def = defaultValueField.getText();

        return switch (itemType) {
            case PLAIN_TEXT -> true;
            case IP_ADDRESS -> def.matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
            case TIME_24H, TIME_12H, TIME_24_HUNDREDS, TIME_DURATION_SECONDS, TIME_DURATION_HUNDREDS, TIME_24H_HHMM, TIME_12H_HHMM ->
                    def.matches("\\d+:\\d+:\\d+(.\\d*)*");
            case GREGORIAN_DATE -> def.matches("\\d+/\\d+/\\d+");
        };
    }
}
