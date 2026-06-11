/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItem;
import com.thecoderscorner.menu.domain.ScrollChoiceMenuItemBuilder;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.StringHelper;
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
    private ComboBox<TidyScrollChoiceValue> modeCombo;
    private TextField defaultValueField;

    public UIScrollChoiceMenuItem(ScrollChoiceMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                  BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.CHOICE_URL);
    }

    @Override
    protected Optional<ScrollChoiceMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        var numItems = safeIntFromProperty(numItemsField.textProperty(), "Num Items", errors, 0, 255);
        var builder = new ScrollChoiceMenuItemBuilder()
                .withExisting(getMenuItem())
                .withChoiceMode(modeCombo.getValue().mode())
                .withNumEntries(numItems);

        if(modeCombo.getValue().mode() != ScrollChoiceMode.CUSTOM_RENDERFN) {
            var width = safeIntFromProperty(itemWidthField.textProperty(), bundle.getString("menu.editor.item.width"), errors, 1, 255);
            builder.withItemWidth(width);
        }

        if(modeCombo.getValue().mode() == ScrollChoiceMode.ARRAY_IN_EEPROM) {
            var eepromOffset = safeIntFromProperty(eepromOffsetField.textProperty(), bundle.getString("menu.editor.eeprom.offset"), errors, 0, 65000);
            builder.withEepromOffset(eepromOffset);
        }

        if(modeCombo.getValue().mode() == ScrollChoiceMode.ARRAY_IN_RAM) {
            var variable = safeStringFromProperty(variableField.textProperty(), bundle.getString("menu.editor.ram.variable"), errors, 64, StringFieldType.VARIABLE);
            builder.withVariable(variable);
        }

        getChangedDefaults(builder, errors);

        String defValStr = defaultValueField.getText();
        String defValField = bundle.getString("menu.editor.default.value");
        try {
            int value = StringHelper.isStringEmptyOrNull(defValStr) ? 0 : Integer.parseInt(defValStr);
            if (value < 0 || value > numItems) {
                errors.add(new FieldError(bundle.getString("menu.editor.err.analog.range") + " " + numItems, defValField));
            } else {
                MenuItemHelper.setMenuState(getMenuItem(), value, menuTree);
            }
        } catch(Exception ex) {
            errors.add(new FieldError(bundle.getString("menu.editor.err.value.parse") + " " + ex.getClass().getSimpleName() + " " + ex.getMessage(), defValField));
        }
        return getItemOrReportError(builder.menuItem(), errors);
    }

    void enableNeededFieldsBasedOnMode(ScrollChoiceMode newMode) {
        itemWidthField.setDisable(newMode == ScrollChoiceMode.CUSTOM_RENDERFN);
        eepromOffsetField.setDisable(newMode != ScrollChoiceMode.ARRAY_IN_EEPROM);
        variableField.setDisable(newMode != ScrollChoiceMode.ARRAY_IN_RAM);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Mode"), 0, idx);
        modeCombo = new ComboBox<>(FXCollections.observableList(List.of(
                new TidyScrollChoiceValue(ScrollChoiceMode.ARRAY_IN_EEPROM, bundle.getString("menu.editor.scroll.choice.eeprom")),
                new TidyScrollChoiceValue(ScrollChoiceMode.ARRAY_IN_RAM, bundle.getString("menu.editor.scroll.choice.ram")),
                new TidyScrollChoiceValue(ScrollChoiceMode.CUSTOM_RENDERFN, bundle.getString("menu.editor.scroll.choice.custom"))
        )));
        modeCombo.getSelectionModel().select(choiceToIndex(getMenuItem().getChoiceMode()));
        modeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
            enableNeededFieldsBasedOnMode(newValue.mode());
            callChangeConsumer();
        });
        modeCombo.setId("choiceModeCombo");
        grid.add(modeCombo, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.initial.items")), 0, idx);
        numItemsField = new TextField(String.valueOf(getMenuItem().getNumEntries()));
        numItemsField.textProperty().addListener(this::coreValueChanged);
        numItemsField.setId("numItemsFieldField");
        TextFormatterUtils.applyIntegerFormatToField(numItemsField);
        grid.add(numItemsField, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.item.width")), 0, idx);
        itemWidthField = new TextField(String.valueOf(getMenuItem().getItemWidth()));
        itemWidthField.textProperty().addListener(this::coreValueChanged);
        itemWidthField.setId("itemWidthFieldField");
        TextFormatterUtils.applyIntegerFormatToField(itemWidthField);
        grid.add(itemWidthField, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.eeprom.offset")), 0, idx);
        eepromOffsetField = new TextField(String.valueOf(getMenuItem().getEepromOffset()));
        eepromOffsetField.textProperty().addListener(this::coreValueChanged);
        eepromOffsetField.setId("eepromOffsetFieldField");
        TextFormatterUtils.applyIntegerFormatToField(eepromOffsetField);
        grid.add(eepromOffsetField, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.ram.variable")), 0, idx);
        variableField = new TextField(getMenuItem().getVariable());
        variableField.setId("choiceVarField");
        variableField.textProperty().addListener(this::coreValueChanged);
        grid.add(variableField, 1, idx);

        enableNeededFieldsBasedOnMode(getMenuItem().getChoiceMode());

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.default.value")), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, new CurrentScrollPosition(0, ""));
        defaultValueField = new TextField(Integer.toString(value.getPosition()));
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        defaultValueField.setId("defaultValueField");
        TextFormatterUtils.applyIntegerFormatToField(defaultValueField);
        grid.add(defaultValueField, 1, idx);

        idx++;

        return idx;
    }

    private int choiceToIndex(ScrollChoiceMode choiceMode) {
        return switch(choiceMode) {
            case ARRAY_IN_EEPROM -> 0;
            case ARRAY_IN_RAM -> 1;
            case CUSTOM_RENDERFN -> 2;
        };
    }

    public record TidyScrollChoiceValue (ScrollChoiceMode mode, String name) {
        @Override
        public String toString() {
            return name;
        }
    }
}
