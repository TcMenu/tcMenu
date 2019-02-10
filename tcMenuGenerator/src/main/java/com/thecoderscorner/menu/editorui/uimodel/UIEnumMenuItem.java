/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uimodel;

import com.thecoderscorner.menu.domain.EnumMenuItem;
import com.thecoderscorner.menu.domain.EnumMenuItemBuilder;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.project.MenuIdChooser;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public class UIEnumMenuItem extends UIMenuItem<EnumMenuItem> {
    private ListView<String> listView;

    public UIEnumMenuItem(EnumMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
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

        EnumMenuItemBuilder builder = EnumMenuItemBuilder.anEnumMenuItemBuilder().withExisting(getMenuItem())
                .withEnumList(items);

        getChangedDefaults(builder, errors);

        return getItemOrReportError(builder.menuItem(), errors);
    }

    @Override
    protected void internalInitPanel(GridPane grid, int idx) {
        idx++;
        grid.add(new Label("Values"), 0, idx);
        ObservableList<String> list = FXCollections.observableArrayList(getMenuItem().getEnumEntries());
        listView = new ListView<>(list);
        listView.setEditable(true);

        listView.setCellFactory(TextFieldListCell.forListView());

        listView.setOnEditCommit(t -> {
            listView.getItems().set(t.getIndex(), t.getNewValue());
        });

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
                list.remove(selectedItem);
            }
        });

        list.addListener((ListChangeListener<? super String>) observable -> callChangeConsumer());

        listView.setId("enumList");
        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                removeButton.setDisable(newValue == null)
        );
        listView.getSelectionModel().selectFirst();
    }
}
