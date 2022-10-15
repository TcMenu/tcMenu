/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItemBuilder;
import com.thecoderscorner.menu.domain.state.PortableColor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import javax.sound.sampled.Port;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIRgb32MenuItem extends UIMenuItem<Rgb32MenuItem> {

    private CheckBox alphaCheck;
    private TextField defaultValueField;

    public UIRgb32MenuItem(Rgb32MenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.RGB_URL);
    }

    @Override
    protected Optional<Rgb32MenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        var alphaEnabled = alphaCheck.isSelected();
        var builder = new Rgb32MenuItemBuilder()
                .withExisting(getMenuItem())
                .withAlpha(alphaEnabled);
        getChangedDefaults(builder, errors);

        try {
            MenuItemHelper.setMenuState(getMenuItem(), defaultValueField.getText(), menuTree);
        } catch(Exception ex) {
            errors.add(new FieldError("Value could not be parsed " + ex.getClass().getSimpleName() + " " + ex.getMessage(), "DefaultValue"));
        }

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Default Value (HTML)"), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, PortableColor.BLACK);
        defaultValueField = new TextField(getMenuItem().isIncludeAlphaChannel() ? value.toString() : value.toString().substring(0, 7));
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        TextFormatterUtils.applyFormatToField(defaultValueField, TextFormatterUtils.PORTABLE_COLOR_MATCH);
        grid.add(defaultValueField, 1, idx);

        idx++;
        alphaCheck = new CheckBox("Enable alpha channel");
        alphaCheck.setId("alphaCheck");
        alphaCheck.setSelected(getMenuItem().isIncludeAlphaChannel());
        alphaCheck.selectedProperty().addListener((observableValue, aBoolean, t1) -> callChangeConsumer());
        grid.add(alphaCheck, 1, idx);

        return idx;
    }
}
