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
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
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
            new AnalogCannedChoice("Integer percentage 0% - 100%", 0, 1, 100, "%"),
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

    public UIAnalogMenuItem(AnalogMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator nameGen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, nameGen, changeConsumer, UrlsForDocumentation.ANALOG_URL);
    }

    @Override
    protected Optional<AnalogMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        String unitName = safeStringFromProperty(unitNameField.textProperty(), "Unit Name", errors, 4, StringFieldType.OPTIONAL);
        int divisor = safeIntFromProperty(divisorField.textProperty(), "Divisor", errors, 0, 10000);
        int offset = safeIntFromProperty(offsetField.textProperty(), "Offset", errors, Short.MIN_VALUE, Short.MAX_VALUE);
        int step = safeIntFromProperty(stepField.textProperty(), "Step", errors, 1, 128);
        int maxValue = safeIntFromProperty(maxValueField.textProperty(), "Maximum Value", errors, 1, 65535);
        if((maxValue % step) != 0) {
            errors.add(new FieldError("'Step' must be exactly divisible by 'Maximum Value'", "Step"));
        }

        AnalogMenuItemBuilder builder = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(getMenuItem())
                .withOffset(offset)
                .withMaxValue(maxValue)
                .withDivisor(divisor)
                .withStep(step)
                .withUnit(unitName);
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
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
        grid.add(new Label("Pre-made starters"), 0, idx);
        grid.add(cannedChoicesCombo, 1, idx);

        idx++;
        grid.add(new Label("Offset from zero"), 0, idx);
        offsetField = new TextField(String.valueOf(getMenuItem().getOffset()));
        offsetField.setId("offsetField");
        offsetField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(offsetField);
        grid.add(offsetField, 1, idx);

        idx++;
        grid.add(new Label("Maximum value"), 0, idx);
        maxValueField = new TextField(String.valueOf(getMenuItem().getMaxValue()));
        maxValueField.setId("maxValueField");
        maxValueField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(maxValueField);
        grid.add(maxValueField, 1, idx);

        idx++;
        grid.add(new Label("Divisor"), 0, idx);
        divisorField = new TextField(String.valueOf(getMenuItem().getDivisor()));
        divisorField.setId("divisorField");
        divisorField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(divisorField);
        grid.add(divisorField, 1, idx);

        idx++;
        grid.add(new Label("Step"), 0, idx);
        stepField = new TextField(String.valueOf(getMenuItem().getStep()));
        stepField.setId("stepField");
        stepField.textProperty().addListener(this::analogValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(stepField);
        grid.add(stepField, 1, idx);

        idx++;
        grid.add(new Label("Unit name"), 0, idx);
        unitNameField = new TextField(getMenuItem().getUnitName());
        unitNameField.setId("unitNameField");
        unitNameField.textProperty().addListener(this::analogValueChanged);
        grid.add(unitNameField, 1, idx);

        return idx;
    }

    protected void analogValueChanged(Observable observable, String oldVal, String newVal) {
        coreValueChanged(observable, oldVal, newVal);
        populateMinMaxLabel();
    }


        private void populateMinMaxLabel() {
        minMaxLabel.setText(String.format("Min value: %s. Max value %s.",
                MenuItemFormatter.formatForDisplay(getMenuItem(), 0),
                MenuItemFormatter.formatForDisplay(getMenuItem(), getMenuItem().getMaxValue())
        ));
    }

    public record AnalogCannedChoice(String name, int offset, int divisor, int maxValue, String unit) {
        @Override
        public String toString() {
            return name;
        }
    }
}
