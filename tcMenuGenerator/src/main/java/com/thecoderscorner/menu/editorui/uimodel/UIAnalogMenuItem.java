/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIAnalogMenuItem extends UIMenuItem<AnalogMenuItem> {

    public static final List<AnalogCannedChoice> CANNED_CHOICES = List.of(
            new AnalogCannedChoice("[Choose one]", 0, 0, 0, ""),
            new AnalogCannedChoice("Integer 0 - 255", 0, 1, 255, ""),
            new AnalogCannedChoice("Integer 1 - 512", 1, 1, 511, ""),
            new AnalogCannedChoice("Integer 0% - 100%", 0, 1, 100, "%"),
            new AnalogCannedChoice("Tenths 0.0 - 100.0", 0, 10, 1000, ""),
            new AnalogCannedChoice("Hundredths 0.00A - 10.00A", 0, 100, 1000, "A"),
            new AnalogCannedChoice("Halves 0.0N - 10.00N", 0, 2, 20, "N"),
            new AnalogCannedChoice("Volume control -90.0dB to 37.5dB", -180, 2, 255, "dB"),
            new AnalogCannedChoice("Negative Tenths -5.0V - 0.0V", -50, 10, 50, "V")
    );

    private TextField offsetField;
    private TextField maxValueField;

    private TextField stepField;
    private TextField divisorField;
    private TextField unitNameField;
    private Label minMaxLabel;
    private ComboBox<AnalogCannedChoice> cannedChoicesCombo;
    private TextField defaultValueField;
    private Label realInterpretationField;

    public UIAnalogMenuItem(AnalogMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator nameGen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, nameGen, changeConsumer, UrlsForDocumentation.ANALOG_URL);
    }

    @Override
    protected Optional<AnalogMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        String unitName = safeStringFromProperty(unitNameField.textProperty(), bundle.getString("menu.editor.analog.unit"), errors, 4, StringFieldType.OPTIONAL);
        int divisor = safeIntFromProperty(divisorField.textProperty(), bundle.getString("menu.editor.analog.divisor"), errors, 0, 10000);
        int offset = safeIntFromProperty(offsetField.textProperty(), bundle.getString("menu.editor.analog.offset"), errors, Short.MIN_VALUE, Short.MAX_VALUE);
        String stepStr = bundle.getString("menu.editor.analog.step");
        int step = safeIntFromProperty(stepField.textProperty(), stepStr, errors, 1, 128);
        int maxValue = safeIntFromProperty(maxValueField.textProperty(), bundle.getString("menu.editor.analog.max"), errors, 1, 65535);
        if(step == 0 || ((maxValue % step) != 0)) {
            errors.add(new FieldError(bundle.getString("menu.editor.err.step.wrong"), stepStr));
        }

        if(localHandler.isLocalSupportEnabled() && !localHandler.getCurrentLocale().getLanguage().equals("--")) {
            var unitResStr = menuItemToLocale("unit");
            localHandler.setLocalSpecificEntry(unitResStr.substring(1), unitName);
            unitName = unitResStr;
        }

        AnalogMenuItemBuilder builder = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(getMenuItem())
                .withOffset(offset)
                .withMaxValue(maxValue)
                .withDivisor(divisor)
                .withStep(step)
                .withUnit(unitName);
        getChangedDefaults(builder, errors);
        var item = builder.menuItem();

        var defValueStr = bundle.getString("menu.editor.default.value");
        try {
            String text = defaultValueField.getText();
            int value = StringHelper.isStringEmptyOrNull(text) ? 0 : Integer.parseInt(text);
            if(value < 0 || value > maxValue) {
                errors.add(new FieldError(bundle.getString("menu.editor.err.analog.range") + maxValue, defValueStr));
            } else {
                MenuItemHelper.setMenuState(item, value, menuTree);
                var fmt = new MenuItemFormatter(localHandler);
                realInterpretationField.setText(fmt.formatForDisplay(item, MenuItemHelper.getValueFor(item, menuTree, 0)));
            }
        } catch (NumberFormatException e) {
            errors.add(new FieldError(bundle.getString("menu.editor.err.value.parse") + " " +
                    e.getClass().getSimpleName() + " " + e.getMessage(), defValueStr));
        }

        return getItemOrReportError(item, errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        minMaxLabel = new Label("");
        populateMinMaxLabel();
        grid.add(minMaxLabel, 1, idx);
        minMaxLabel.setId("minMaxLabel");

        idx++;
        cannedChoicesCombo = new ComboBox<>();
        cannedChoicesCombo.setMaxWidth(9999);
        cannedChoicesCombo.setItems(FXCollections.observableList(CANNED_CHOICES));
        cannedChoicesCombo.getSelectionModel().select(0);
        cannedChoicesCombo.setId("cannedChoicesCombo");
        cannedChoicesCombo.setOnAction(event -> {
            var selected = cannedChoicesCombo.getSelectionModel().getSelectedItem();
            if(selected != null && cannedChoicesCombo.getSelectionModel().getSelectedIndex() > 0) {
                offsetField.setText(String.valueOf(selected.offset()));
                maxValueField.setText(String.valueOf(selected.maxValue()));
                divisorField.setText(String.valueOf(selected.divisor()));
                unitNameField.setText(selected.unit());
                analogValueChanged(unitNameField.textProperty(), "", "");
            }
        });
        grid.add(new Label(bundle.getString("menu.editor.analog.pre.made")), 0, idx);
        grid.add(cannedChoicesCombo, 1, idx, 2, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.analog.offset")), 0, idx);
        offsetField = new TextField(String.valueOf(getMenuItem().getOffset()));
        offsetField.setId("offsetField");
        offsetField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(offsetField);
        grid.add(offsetField, 1, idx, 2, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.analog.max")), 0, idx);
        maxValueField = new TextField(String.valueOf(getMenuItem().getMaxValue()));
        maxValueField.setId("maxValueField");
        maxValueField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(maxValueField);
        grid.add(maxValueField, 1, idx, 2, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.analog.divisor")), 0, idx);
        divisorField = new TextField(String.valueOf(getMenuItem().getDivisor()));
        divisorField.setId("divisorField");
        divisorField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(divisorField);
        grid.add(divisorField, 1, idx, 2, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.analog.step")), 0, idx);
        stepField = new TextField(String.valueOf(getMenuItem().getStep()));
        stepField.setId("stepField");
        stepField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(stepField);
        grid.add(stepField, 1, idx, 2, 1);

        String unitName = getMenuItem().getUnitName();
        if(localHandler.isLocalSupportEnabled() && !localHandler.getCurrentLocale().getLanguage().equals("--")) {
            unitName = localHandler.getFromLocaleWithDefault(menuItemToLocale("unit"), unitName);
        }

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.analog.unit")), 0, idx);
        unitNameField = new TextField(unitName);
        unitNameField.setId("unitNameField");
        unitNameField.textProperty().addListener(this::analogValueChanged);
        grid.add(unitNameField, 1, idx, 2, 1);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.default.value")), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, 0);
        realInterpretationField = new Label();
        MenuItemFormatter fmt= new MenuItemFormatter(localHandler);
        realInterpretationField.setText(fmt.formatForDisplay(getMenuItem(), value));
        grid.add(realInterpretationField, 2, idx);
        defaultValueField = new TextField(Integer.toString(value));
        defaultValueField.setId("defaultValueField");
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        TextFormatterUtils.applyIntegerFormatToField(defaultValueField);
        grid.add(defaultValueField, 1, idx, 1, 1);

        return idx;
    }

    protected void analogValueChanged(Observable observable, String oldVal, String newVal) {
        coreValueChanged(observable, oldVal, newVal);
        populateMinMaxLabel();
    }

    @Override
    protected void localeDidChange() {
        String unitName = getMenuItem().getUnitName();
        if(localHandler.isLocalSupportEnabled() && !localHandler.getCurrentLocale().getLanguage().equals("--")) {
            unitName = localHandler.getFromLocaleWithDefault(menuItemToLocale("unit"), unitName);
        }
        unitNameField.setText(unitName);
    }

    private void populateMinMaxLabel() {
        var fmt = new MenuItemFormatter(localHandler);
        minMaxLabel.setText(String.format(bundle.getString("menu.editor.analog.min.max.fmt"),
                fmt.formatForDisplay(getMenuItem(), 0),
                fmt.formatForDisplay(getMenuItem(), getMenuItem().getMaxValue())
        ));
    }

    public record AnalogCannedChoice(String name, int offset, int divisor, int maxValue, String unit) {
        @Override
        public String toString() {
            return name;
        }
    }
}
