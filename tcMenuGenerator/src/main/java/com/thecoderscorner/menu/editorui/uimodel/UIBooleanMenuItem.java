/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.BooleanMenuItem;
import com.thecoderscorner.menu.domain.BooleanMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
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
import static com.thecoderscorner.menu.domain.BooleanMenuItem.BooleanNaming.*;
import static com.thecoderscorner.menu.domain.BooleanMenuItemBuilder.aBooleanMenuItemBuilder;

public class UIBooleanMenuItem extends UIMenuItem<BooleanMenuItem> {

    public static final TidyBooleanNaming TIDY_NAMING_TRUE_FALSE = new TidyBooleanNaming(TRUE_FALSE, "TRUE / FALSE");
    public static final TidyBooleanNaming TIDY_NAMING_ON_OFF = new TidyBooleanNaming(ON_OFF, "ON / OFF");
    public static final TidyBooleanNaming TIDY_NAMING_YES_NO = new TidyBooleanNaming(YES_NO, "YES / NO");
    public static final TidyBooleanNaming TIDY_NAMING_CHECKBOX = new TidyBooleanNaming(CHECKBOX, "CheckBox");

    private ComboBox<TidyBooleanNaming> namingBox;
    private ComboBox<BooleanNamingValue> defaultValue;

    public UIBooleanMenuItem(BooleanMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.BOOLEAN_URL);
    }

    @Override
    protected Optional<BooleanMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        BooleanMenuItemBuilder builder = aBooleanMenuItemBuilder().withExisting(getMenuItem())
                .withNaming(namingBox.getValue().naming());

        boolean isDefaultValueTrue = defaultValue.getSelectionModel().getSelectedIndex() == 0;
        MenuItemHelper.setMenuState(getMenuItem(), isDefaultValueTrue, menuTree);

        getChangedDefaults(builder, errors);
        return getItemOrReportError(builder.menuItem(), errors);
    }

    int namingToIndex(BooleanNaming naming) {
        return switch (naming) {
            case TRUE_FALSE -> 0;
            case ON_OFF -> 1;
            case YES_NO -> 2;
            case CHECKBOX -> 3;
        };
    }

    @Override
    protected int internalInitPanel(GridPane pane, int idx) {
        idx++;
        pane.add(new Label("Possible responses"), 0, idx);
        ObservableList<TidyBooleanNaming> list = FXCollections.observableList(List.of(
                TIDY_NAMING_TRUE_FALSE,
                TIDY_NAMING_ON_OFF,
                TIDY_NAMING_YES_NO,
                TIDY_NAMING_CHECKBOX
        ));
        namingBox = new ComboBox<>(list);
        namingBox.getSelectionModel().select(namingToIndex((getMenuItem().getNaming())));
        namingBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            callChangeConsumer();
            prepareDefaultValueBasedOnNaming();
        });
        namingBox.setId("booleanNamingCombo");
        namingBox.setMaxWidth(9999);
        pane.add(namingBox, 1, idx, 2, 1);
        idx++;

        pane.add(new Label("Default Value"), 0, idx);
        defaultValue = new ComboBox<>();
        prepareDefaultValueBasedOnNaming();
        defaultValue.setOnAction(event -> callChangeConsumer());
        defaultValue.setId("defaultValueCombo");
        defaultValue.setMaxWidth(9999);
        pane.add(defaultValue, 1, idx, 2, 1);

        return idx;
    }

    private void prepareDefaultValueBasedOnNaming() {
        var before = MenuItemHelper.getValueFor(getMenuItem(), menuTree, false) ? 0 : 1;
        defaultValue.setItems(findBooleanNamingListFor(getMenuItem().getNaming()));
        defaultValue.getSelectionModel().select(before);
    }

    private ObservableList<BooleanNamingValue> findBooleanNamingListFor(BooleanNaming naming) {
        return switch (naming) {
            case CHECKBOX -> FXCollections.observableArrayList(new BooleanNamingValue("Checked", true), new BooleanNamingValue("Unchecked", false));
            case ON_OFF -> FXCollections.observableArrayList(new BooleanNamingValue("On", true), new BooleanNamingValue("Off", false));
            case YES_NO -> FXCollections.observableArrayList(new BooleanNamingValue("Yes", true), new BooleanNamingValue("No", false));
            case TRUE_FALSE -> FXCollections.observableArrayList(new BooleanNamingValue("True", true), new BooleanNamingValue("False", false));
        };
    }

    public record TidyBooleanNaming(BooleanNaming naming, String name) {
        @Override
        public String toString() {
            return name;
        }
    }

    public record BooleanNamingValue(String name, boolean value) {
        @Override
        public String toString() {
            return name;
        }
    }
}
