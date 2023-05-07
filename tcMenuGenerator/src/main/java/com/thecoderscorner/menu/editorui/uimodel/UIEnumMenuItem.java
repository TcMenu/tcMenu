/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.editorui.generator.core.VariableNameGenerator;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIEnumMenuItem extends UIMenuItem<EnumMenuItem> {
    private ListView<String> listView;
    private TextField defaultValueField;

    public UIEnumMenuItem(EnumMenuItem menuItem, MenuIdChooser chooser, VariableNameGenerator gen, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, gen, changeConsumer, UrlsForDocumentation.ENUM_URL);
    }

    @Override
    protected Optional<EnumMenuItem> getChangedMenuItem() {
        List<FieldError> errors = new ArrayList<>();

        ObservableList<String> items = listView.getItems();
        if (items.isEmpty()) {
            errors.add(new FieldError(bundle.getString("menu.editor.enum.no.choices"), "Choices"));
        } else if (items.stream().anyMatch(str -> str.isEmpty() || str.matches(".*[\"\\\\].*$"))) {
            errors.add(new FieldError(bundle.getString("menu.editor.enum.fmt.error"), "Choices"));
        }

        EnumMenuItemBuilder builder = EnumMenuItemBuilder.anEnumMenuItemBuilder().withExisting(getMenuItem());
        builder.withEnumList(getValueLocalizedFromUIList(items));
        getChangedDefaults(builder, errors);

        var defValue = bundle.getString("menu.editor.default.value");
        try {
            String text = defaultValueField.getText();
            int value = StringHelper.isStringEmptyOrNull(text) ? 0 : Integer.parseInt(text);
            if (value < 0 || value > items.size()) {
                errors.add(new FieldError(bundle.getString("menu.editor.err.analog.range") + " " + items.size(), defValue));
            } else {
                MenuItemHelper.setMenuState(getMenuItem(), value, menuTree);
            }
        } catch (Exception ex) {
            errors.add(new FieldError(bundle.getString("menu.editor.err.value.parse") + ex.getClass().getSimpleName() + " " + ex.getMessage(), defValue));
        }

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label(bundle.getString("menu.editor.enum.values")), 0, idx);
        List<String> enumEntries = getMenuItem().getEnumEntries();
        ObservableList<String> list = FXCollections.observableArrayList(enumEntries);
        listView = new ListView<>(list);
        createLocalizedList(listView, enumEntries, list);
        grid.add(listView, 1, idx, 1, 3);
        idx+=3;
        prepareAddRemoveButtons(listView, grid, idx);

        idx++;
        grid.add(new Label(bundle.getString("menu.editor.default.value")), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, 0);
        defaultValueField = new TextField(Integer.toString(value));
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        defaultValueField.setId("defaultValueField");
        TextFormatterUtils.applyIntegerFormatToField(defaultValueField);
        grid.add(defaultValueField, 1, idx);

        list.addListener((ListChangeListener<? super String>) observable -> callChangeConsumer());

        return idx;
    }

    protected String getEnumEntryKey(int i) {
        return String.format("menu.%d.enum.%d", getMenuItem().getId(), i);
    }
}
