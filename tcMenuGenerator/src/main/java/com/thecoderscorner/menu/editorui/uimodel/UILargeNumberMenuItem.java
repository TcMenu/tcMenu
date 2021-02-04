/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UILargeNumberMenuItem extends UIMenuItem<EditableLargeNumberMenuItem> {

    private TextField decimalPlaces;
    private TextField totalDigits;
    private CheckBox negativeAllowedCheck;

    public UILargeNumberMenuItem(EditableLargeNumberMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                 BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer);
    }

    @Override
    protected Optional<EditableLargeNumberMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        var dp = safeIntFromProperty(decimalPlaces.textProperty(), "Decimal Places", errors, 0, 8);
        var tot = safeIntFromProperty(totalDigits.textProperty(), "Total Digits", errors, 4, 12);

        if(tot <= dp) {
            errors.add(new FieldError("Total must be greater than decimal places", "Total Digits"));
        }
        if((tot - dp) > 9) {
            errors.add(new FieldError("Whole part cannot be larger than 9 figures", "Total Digits"));
        }

        var builder = EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                .withExisting(getMenuItem())
                .withNegativeAllowed(negativeAllowedCheck.isSelected())
                .withDecimalPlaces(dp)
                .withTotalDigits(tot);
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Decimal Places"), 0, idx);
        decimalPlaces = new TextField(String.valueOf(getMenuItem().getDecimalPlaces()));
        decimalPlaces.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(decimalPlaces);
        grid.add(decimalPlaces, 1, idx);

        idx++;
        grid.add(new Label("Total Digits"), 0, idx);
        totalDigits = new TextField(String.valueOf(getMenuItem().getDigitsAllowed()));
        totalDigits.textProperty().addListener(this::coreValueChanged);
        TextFormatterUtils.applyIntegerFormatToField(totalDigits);
        grid.add(totalDigits, 1, idx);

        idx++;
        negativeAllowedCheck = new CheckBox("Allow negative values");
        negativeAllowedCheck.setOnAction(this::checkboxChanged);
        negativeAllowedCheck.setId("NegAllowCheck");
        negativeAllowedCheck.setSelected(getMenuItem().isNegativeAllowed());
        grid.add(negativeAllowedCheck, 1, idx);
        return idx;
    }
}
