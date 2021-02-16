/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItemBuilder;
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

import static com.thecoderscorner.menu.domain.ScrollChoiceMenuItem.ScrollChoiceMode;

public class UIScrollChoiceMenuItem extends UIMenuItem<ScrollChoiceMenuItem> {

    private TextField itemWidthField;
    private TextField numItemsField;
    private TextField eepromOffsetField;
    private TextField variableField;
    private ComboBox<ScrollChoiceMode> modeCombo;

    public UIScrollChoiceMenuItem(ScrollChoiceMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                  BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.CHOICE_URL);
    }

    @Override
    protected Optional<ScrollChoiceMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        var width = safeIntFromProperty(itemWidthField.textProperty(), "Item Width", errors, 1, 255);
        var numItems = safeIntFromProperty(numItemsField.textProperty(), "Num Items", errors, 0, 255);
        var eepromOffset = safeIntFromProperty(eepromOffsetField.textProperty(), "EEPROM Offset", errors, 0, 65000);
        var variable = safeStringFromProperty(variableField.textProperty(), "Variable Name", errors, 64, StringFieldType.OPTIONAL);
        var builder = new ScrollChoiceMenuItemBuilder()
                .withExisting(getMenuItem())
                .withChoiceMode(modeCombo.getValue())
                .withItemWidth(width)
                .withEepromOffset(eepromOffset)
                .withNumEntries(numItems)
                .withVariable(variable);
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Mode"), 0, idx);
        modeCombo = new ComboBox<>(FXCollections.observableList(List.of(ScrollChoiceMode.values())));
        modeCombo.getSelectionModel().select(getMenuItem().getChoiceMode());
        modeCombo.valueProperty().addListener((observable, oldValue, newValue) -> callChangeConsumer());
        grid.add(modeCombo, 1, idx);

        idx++;
        grid.add(new Label("Item Width"), 0, idx);
        itemWidthField = new TextField(String.valueOf(getMenuItem().getItemWidth()));
        itemWidthField.textProperty().addListener(this::coreValueChanged);
        itemWidthField.setId("itemWidthFieldField");
        TextFormatterUtils.applyIntegerFormatToField(itemWidthField);
        grid.add(itemWidthField, 1, idx);

        idx++;
        grid.add(new Label("Initial Items"), 0, idx);
        numItemsField = new TextField(String.valueOf(getMenuItem().getNumEntries()));
        numItemsField.textProperty().addListener(this::coreValueChanged);
        numItemsField.setId("numItemsFieldField");
        TextFormatterUtils.applyIntegerFormatToField(numItemsField);
        grid.add(numItemsField, 1, idx);

        idx++;
        grid.add(new Label("EEPROM Offset (Rom only)"), 0, idx);
        eepromOffsetField = new TextField(String.valueOf(getMenuItem().getEepromOffset()));
        eepromOffsetField.textProperty().addListener(this::coreValueChanged);
        eepromOffsetField.setId("eepromOffsetFieldField");
        TextFormatterUtils.applyIntegerFormatToField(eepromOffsetField);
        grid.add(eepromOffsetField, 1, idx);

        idx++;
        grid.add(new Label("Variable (RAM only)"), 0, idx);
        variableField = new TextField(getMenuItem().getVariable());
        variableField.setId("varField");
        variableField.textProperty().addListener(this::coreValueChanged);
        grid.add(variableField, 1, idx);

        idx++;

        return idx;
    }
}
