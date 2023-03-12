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


        if(items.isEmpty()) {
            errors.add(new FieldError("There must be at least one choice", "Choices"));
        }
        else if(items.stream().anyMatch(str-> str.isEmpty() || str.matches(".*[\"\\\\].*$"))) {
            errors.add(new FieldError("Choices must not contain speech marks or backslash", "Choices"));
        }

        EnumMenuItemBuilder builder = EnumMenuItemBuilder.anEnumMenuItemBuilder().withExisting(getMenuItem());

        if(localHandler.isLocalSupportEnabled() && !localHandler.getCurrentLocale().getLanguage().equals("--")) {
            var itemsLocaleList = new ArrayList<String>();
            for(int i=0; i<items.size(); i++) {
                String enumEntryName = getEnumEntryKey(i);
                localHandler.setLocalSpecificEntry(enumEntryName, items.get(i));
                itemsLocaleList.add("%" + enumEntryName);
            }
            builder.withEnumList(itemsLocaleList);
        } else {
            builder.withEnumList(items);
        }

        getChangedDefaults(builder, errors);

        try {
            String text = defaultValueField.getText();
            int value = StringHelper.isStringEmptyOrNull(text) ? 0 : Integer.parseInt(text);
            if (value < 0 || value > items.size()) {
                errors.add(new FieldError("Value must be between 0 and " + items.size(), "DefaultValue"));
            } else {
                MenuItemHelper.setMenuState(getMenuItem(), value, menuTree);
            }
        } catch(Exception ex) {
            errors.add(new FieldError("Value could not be parsed " + ex.getClass().getSimpleName() + " " + ex.getMessage(), "DefaultValue"));
        }

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected int internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Values"), 0, idx);
        List<String> enumEntries = getMenuItem().getEnumEntries();
        ObservableList<String> list = FXCollections.observableArrayList(enumEntries);
        if(localHandler.isLocalSupportEnabled() && !localHandler.getCurrentLocale().getLanguage().equals("--")) {
            var itemsLocaleList = new ArrayList<String>();
            for(int i=0; i<enumEntries.size(); i++) {
                String enumEntryName = getEnumEntryKey(i);
                itemsLocaleList.add(localHandler.getWithLocaleInitIfNeeded("%" + enumEntryName, enumEntries.get(i)));
            }
            list = FXCollections.observableList(itemsLocaleList);
        }
        listView = new ListView<>(list);
        listView.setEditable(true);
        listView.setPrefHeight(120);

        listView.setCellFactory(TextFieldListCell.forListView());

        listView.setOnEditCommit(t -> listView.getItems().set(t.getIndex(), t.getNewValue()));

        listView.setMinHeight(100);
        grid.add(listView, 1, idx, 1, 3);
        idx+=3;
        Button addButton = new Button("Add");
        addButton.setId("addEnumEntry");
        Button removeButton = new Button("Remove");
        removeButton.setId("removeEnumEntry");
        removeButton.setDisable(true);
        HBox hbox = new HBox(addButton, removeButton);
        grid.add(hbox, 1, idx);

        addButton.setOnAction(event -> {
            listView.getItems().add("ChangeMe");
            listView.getSelectionModel().selectLast();
        });

        removeButton.setOnAction(event -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                listView.getItems().remove(selectedItem);
            }
        });

        list.addListener((ListChangeListener<? super String>) observable -> callChangeConsumer());

        listView.setId("enumList");
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                removeButton.setDisable(newValue == null)
        );
        listView.getSelectionModel().selectFirst();

        idx++;
        grid.add(new Label("Default index (0 based)"), 0, idx);
        var value = MenuItemHelper.getValueFor(getMenuItem(), menuTree, 0);
        defaultValueField = new TextField(Integer.toString(value));
        defaultValueField.textProperty().addListener(e -> callChangeConsumer());
        defaultValueField.setId("defaultValueField");
        TextFormatterUtils.applyIntegerFormatToField(defaultValueField);
        grid.add(defaultValueField, 1, idx);

        return idx;
    }

    private String getEnumEntryKey(int i) {
        return String.format("menu.%d.enum.%d", getMenuItem().getId(), i);
    }
}
