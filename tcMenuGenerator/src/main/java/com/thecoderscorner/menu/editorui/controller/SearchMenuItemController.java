package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.persist.LocaleMappingHandler;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;

import java.util.Optional;

public class SearchMenuItemController {
    public TableView<MenuItem> resultsTable;
    public TableColumn<MenuItem, String> subColumn;
    public TableColumn<MenuItem, String> itemColumn;
    public TableColumn<MenuItem, String> typeColumn;
    public TextField searchField;
    private MenuTree tree;
    private Optional<MenuItem> result = Optional.empty();
    private LocaleMappingHandler handler;

    public void init(MenuTree menuTree, LocaleMappingHandler handler) {
        this.handler = handler;
        this.tree = menuTree;
        searchField.textProperty().addListener((observable, oldValue, newValue) -> searchHasChanged());

        resultsTable.setOnMousePressed(event -> {
            if (event.isPrimaryButtonDown() && event.getClickCount() == 2) {
                assignResult();
            }
        });
        resultsTable.setOnKeyReleased(event -> {
            if(event.getCode() == KeyCode.ENTER) {
                assignResult();
            }
        });
        subColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(handler.getFromLocaleOrUseSource(tree.findParent(cell.getValue()).getName())));
        itemColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(handler.getFromLocaleOrUseSource(cell.getValue().getName())));
        typeColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().getClass().getSimpleName()));
    }

    private void assignResult() {
        result = Optional.ofNullable(resultsTable.getSelectionModel().getSelectedItem());
        closeWindow();

    }

    private void closeWindow() {
        ((Stage)resultsTable.getScene().getWindow()).close();
    }

    private void searchHasChanged() {
        var s = searchField.getText();
        if(s.isEmpty()) {
            resultsTable.setItems(FXCollections.emptyObservableList());
            return;
        }

        if(s.matches("^\\d+$")) {
            var id = Integer.parseInt(s);
            var maybeItem = tree.getMenuById(id);
            maybeItem.ifPresent(item -> resultsTable.setItems(FXCollections.singletonObservableList(item)));

        } else {
            var filteredItems = tree.getAllMenuItems().stream()
                    .filter(it -> it.getName().toLowerCase().contains(s.toLowerCase()))
                    .toList();
            resultsTable.setItems(FXCollections.observableArrayList(filteredItems));
        }
    }

    public Optional<MenuItem> getResult() {
        return result;
    }

    public void onCancel(ActionEvent actionEvent) {
        closeWindow();
    }
}
