/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
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

public class UIAnalogMenuItem extends UIMenuItem<AnalogMenuItem> {

    private TextField offsetField;
    private TextField maxValueField;
    private TextField divisorField;
    private TextField unitNameField;

    public UIAnalogMenuItem(AnalogMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator nameGen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, nameGen, changeConsumer, UrlsForDocumentation.ANALOG_URL);
    }

    @Override
    protected Optional<AnalogMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        String unitName = safeStringFromProperty(unitNameField.textProperty(), "Unit Name", errors, 4, StringFieldType.OPTIONAL);
        int divisor = safeIntFromProperty(divisorField.textProperty(), "Divisor", errors, 0, 10000);
        int offset = safeIntFromProperty(offsetField.textProperty(), "Offset", errors, Short.MIN_VALUE, Short.MAX_VALUE);
        int maxValue = safeIntFromProperty(maxValueField.textProperty(), "Maximum Value", errors, 1, 65535);

        AnalogMenuItemBuilder builder = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(getMenuItem())
                .withOffset(offset)
                .withMaxValue(maxValue)
                .withDivisor(divisor)
                .withUnit(unitName);
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Offset from zero"), 0, idx);
        offsetField = new TextField(String.valueOf(getMenuItem().getOffset()));
        offsetField.setId("offsetField");
        offsetField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(offsetField);
        grid.add(offsetField, 1, idx);

        idx++;
        grid.add(new Label("Maximum value"), 0, idx);
        maxValueField = new TextField(String.valueOf(getMenuItem().getMaxValue()));
        maxValueField.setId("maxValueField");
        maxValueField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(maxValueField);
        grid.add(maxValueField, 1, idx);

        idx++;
        grid.add(new Label("Divisor"), 0, idx);
        divisorField = new TextField(String.valueOf(getMenuItem().getDivisor()));
        divisorField.setId("divisorField");
        divisorField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(divisorField);
        grid.add(divisorField, 1, idx);

        idx++;
        grid.add(new Label("Unit name"), 0, idx);
        unitNameField = new TextField(getMenuItem().getUnitName());
        unitNameField.setId("unitNameField");
        unitNameField.textProperty().addListener(this::coreValueChanged);
        grid.add(unitNameField, 1, idx);

        return idx;
    }
}
