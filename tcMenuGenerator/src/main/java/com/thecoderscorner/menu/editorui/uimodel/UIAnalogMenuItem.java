package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.AnalogMenuItem;
import com.thecoderscorner.menu.domain.AnalogMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.function.BiConsumer;

public class UIAnalogMenuItem extends UIMenuItem<AnalogMenuItem> {

    private TextField offsetField;
    private TextField maxValueField;
    private TextField divisorField;
    private TextField unitNameField;

    public UIAnalogMenuItem(AnalogMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected AnalogMenuItem getChangedMenuItem() {
        AnalogMenuItemBuilder builder = AnalogMenuItemBuilder.anAnalogMenuItemBuilder()
                .withExisting(getMenuItem())
                .withOffset(getOffset())
                .withMaxValue(getMaxValue())
                .withDivisor(getDivisor())
                .withUnit(getUnitName());
        getChangedDefaults(builder);
        return builder.menuItem();
    }

    public int getOffset() {
        return safeIntFromProperty(offsetField.textProperty());
    }

    public int getMaxValue() {
        return safeIntFromProperty(maxValueField.textProperty());
    }

    public int getDivisor() {
        return safeIntFromProperty(divisorField.textProperty());
    }

    public String getUnitName() {
        return unitNameField.getText();
    }

    @Override
    protected void internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Offset from zero"), 0, idx);
        offsetField = new TextField(String.valueOf(getMenuItem().getOffset()));
        offsetField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(offsetField);
        grid.add(offsetField, 1, idx);

        idx++;
        grid.add(new Label("Maximum value"), 0, idx);
        maxValueField = new TextField(String.valueOf(getMenuItem().getMaxValue()));
        maxValueField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(maxValueField);
        grid.add(maxValueField, 1, idx);

        idx++;
        grid.add(new Label("Divisor"), 0, idx);
        divisorField = new TextField(String.valueOf(getMenuItem().getDivisor()));
        divisorField.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(divisorField);
        grid.add(divisorField, 1, idx);

        idx++;
        grid.add(new Label("Unit name"), 0, idx);
        unitNameField = new TextField(getMenuItem().getUnitName());
        unitNameField.textProperty().addListener(this::coreValueChanged);
        grid.add(unitNameField, 1, idx);
    }
}
