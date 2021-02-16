/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItem;
import com.thecoderscorner.menu.domain.Rgb32MenuItemBuilder;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIRgb32MenuItem extends UIMenuItem<Rgb32MenuItem> {

    private CheckBox alphaCheck;

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
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        alphaCheck = new CheckBox("Enable alpha channel");
        alphaCheck.setId("alphaCheck");
        alphaCheck.setSelected(getMenuItem().isIncludeAlphaChannel());
        alphaCheck.selectedProperty().addListener((observableValue, aBoolean, t1) -> callChangeConsumer());
        grid.add(alphaCheck, 1, idx);
        return idx;
    }
}
