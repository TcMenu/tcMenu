/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItem;
import com.thecoderscorner.menu.domain.EditableLargeNumberMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UILargeNumberMenuItem extends UIMenuItem<EditableLargeNumberMenuItem> {

    private TextField decimalPlaces;
    private TextField totalDigits;
    private CheckBox negativeAllowedCheck;
    private TextField defaultValueField;

    public UILargeNumberMenuItem(EditableLargeNumberMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen,
                                 BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.LARGE_NUM_URL);
    }

    @Override
    protected Optional<EditableLargeNumberMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();
        var dp = safeIntFromProperty(decimalPlaces.textProperty(), bundle.getString("menu.editor.decimal.places"), errors, 0, 8);
        String totDigitsStr = bundle.getString("menu.editor.total.digits");
        var tot = safeIntFromProperty(totalDigits.textProperty(), totDigitsStr, errors, 4, 12);

        if(tot <= dp) {
            errors.add(new FieldError(bundle.getString("menu.editor.total.digits.greater.dp"), totDigitsStr));
        }
        if((tot - dp) > 9) {
            errors.add(new FieldError(bundle.getString("menu.editor.whole.too.large"), totDigitsStr));
        }

        boolean negAllowed = negativeAllowedCheck.isSelected();
        var builder = EditableLargeNumberMenuItemBuilder.aLargeNumberItemBuilder()
                .withExisting(getMenuItem())
                .withNegativeAllowed(negAllowed)
                .withDecimalPlaces(dp)
                .withTotalDigits(tot);
        getChangedDefaults(builder, errors);

        try {
            String text = defaultValueField.getText();
            var value = StringHelper.isStringEmptyOrNull(text) ? BigDecimal.ZERO : new BigDecimal(text);
            if (value.doubleValue() < 0 && !negAllowed) {
                errors.add(new FieldError(bundle.getString("menu.editor.value.cant.negative"), "DefaultValue"));
            } else {
                MenuItemHelper.setMenuState(getMenuItem(), value, menuTree);
            }
        } catch(Exception ex) {
            errors.add(new FieldError(bundle.getString("menu.editor.err.value.parse") + ex.getClass().getSimpleName() + " " + ex.getMessage(), "DefaultValue"));
        }

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label(bundle.getString("menu.editor.decimal.places")), 0, idx);
        decimalPlaces = new TextField(String.valueOf(getMenuItem().getDecimalPlaces()));
        decimalPlaces.textProperty().addListener(this::coreValueChanged);
        decimalPlaces.setId("lgeDecimalPlaces");
        TextFormatterUtils.applyIntegerFormatToField(decimalPlaces);
        grid.add(decimalPlaces, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.total.digits")), 0, idx);
        totalDigits = new TextField(String.valueOf(getMenuItem().getDigitsAllowed()));
        totalDigits.textProperty().addListener(this::coreValueChanged);
        totalDigits.setId("lgeTotalDigits");
        TextFormatterUtils.applyIntegerFormatToField(totalDigits);
        grid.add(totalDigits, 1, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.default.value")), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, BigDecimal.ZERO);
        defaultValueField = new TextField(value.toString());
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        defaultValueField.setId("defaultValueField");
        TextFormatterUtils.applyFormatToField(defaultValueField, TextFormatterUtils.FLOAT_MATCH);
        grid.add(defaultValueField, 1, idx);

        idx++;
        negativeAllowedCheck = new CheckBox(bundle.getString("menu.editor.allow.negative"));
        negativeAllowedCheck.setOnAction(this::checkboxChanged);
        negativeAllowedCheck.setId("NegAllowCheck");
        negativeAllowedCheck.setSelected(getMenuItem().isNegativeAllowed());
        grid.add(negativeAllowedCheck, 1, idx);

        return idx;
    }
}
