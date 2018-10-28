/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;

public class UIBooleanMenuItem extends UIMenuItem<BooleanMenuItem> {

    private ComboBox<BooleanNaming> namingBox;

    public UIBooleanMenuItem(BooleanMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected Optional<BooleanMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        BooleanMenuItemBuilder builder = aBooleanMenuItemBuilder().withExisting(getMenuItem())
                .withNaming(namingBox.getValue());
        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected void internalInitPanel(GridPane pane, int idx) {
        idx++;
        pane.add(new Label("Responses"), 0, idx);
        ObservableList<BooleanNaming> list = FXCollections.observableArrayList(BooleanNaming.values());
        namingBox = new ComboBox<>(list);
        namingBox.getSelectionModel().select(getMenuItem().getNaming());
        namingBox.valueProperty().addListener((observable, oldValue, newValue) -> callChangeConsumer());
        pane.add(namingBox, 1, idx);
    }
}
