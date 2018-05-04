/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
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

import java.util.function.BiConsumer;

public class UIEnumMenuItem extends UIMenuItem<EnumMenuItem> {
    private ListView<String> listView;

    public UIEnumMenuItem(EnumMenuItem menuItem, MenuIdChooser chooser, BiConsumer<MenuItem, MenuItem> changeConsumer) {
        super(menuItem, chooser, changeConsumer);
    }

    @Override
    protected EnumMenuItem getChangedMenuItem() {
        EnumMenuItemBuilder builder = EnumMenuItemBuilder.anEnumMenuItemBuilder().withExisting(getMenuItem())
                .withEnumList(listView.getItems());
        getChangedDefaults(builder);
        return builder.menuItem();
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
        Button removeButton = new Button("Remove");
        removeButton.setDisable(true);
        HBox hbox = new HBox(addButton, removeButton);
        grid.add(hbox, 1, idx);

        addButton.setOnAction(event -> listView.getItems().add("ChangeMe"));

        removeButton.setOnAction(event -> {
            String selectedItem = listView.getSelectionModel().getSelectedItem();
            if(selectedItem != null) {
                list.remove(selectedItem);
            }
        });

        list.addListener((ListChangeListener<? super String>) observable -> {
            callChangeConsumer();
        });

        listView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
                removeButton.setDisable(newValue == null)
        );
        listView.getSelectionModel().selectFirst();
    }
}
