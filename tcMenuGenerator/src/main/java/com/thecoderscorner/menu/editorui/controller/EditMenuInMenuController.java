package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.dialog.EditMenuInMenuItemDialog;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuCollection;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class EditMenuInMenuController {
    public TableColumn<MenuInMenuDefinition, String> nameCol;
    public TableColumn<MenuInMenuDefinition, String> typeCol;
    public TableColumn<MenuInMenuDefinition, String> submenuCol;
    public TableColumn<MenuInMenuDefinition, String> connectionCol;
    public Button removeButton;
    public Button editButton;
    public TableView<MenuInMenuDefinition> menuInMenuTable;
    private MenuInMenuCollection collection;
    private MenuTree tree;

    public void initialise(MenuInMenuCollection collection, MenuTree tree) {
        this.collection = collection;
        this.tree = tree;

         nameCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().variableName()));
         typeCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().connectionType().toString()));
         submenuCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(tree.getMenuById(cell.getValue().subMenuId()).orElse(MenuTree.ROOT).toString()));
         connectionCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().printableConnection()));
         rebuildTable();
         menuInMenuTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
             editButton.setDisable(newValue == null);
             removeButton.setDisable(newValue == null);
         });
         if(menuInMenuTable.getItems().isEmpty()) {
             editButton.setDisable(true);
             removeButton.setDisable(true);
         } else {
             menuInMenuTable.getSelectionModel().selectFirst();
         }
    }

    public void onlineHelpWasPressed(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(AppInformationPanel.MENU_IN_MENU_GUIDE_PAGE);
    }

    public void closeWasPressed(ActionEvent actionEvent) {
        getStage().close();
    }

    private Stage getStage() {
        return (Stage) editButton.getScene().getWindow();
    }

    public void addWasPressed(ActionEvent actionEvent) {
        var dialog = new EditMenuInMenuItemDialog(getStage(), new MenuInMenuDefinition("New Item", "COM1", 9600,
                MenuInMenuDefinition.ConnectionType.SERIAL, MenuTree.ROOT.getId(), 100000, 65000), tree);
        dialog.getResult().ifPresent(result -> {
            collection.addDefinition(result);
            rebuildTable();
            menuInMenuTable.getSelectionModel().select(result);
        });
    }

    private void rebuildTable() {
        menuInMenuTable.setItems(FXCollections.observableArrayList(collection.getAllDefinitions()));
    }

    public void removeWasPressed(ActionEvent actionEvent) {
        var selected = menuInMenuTable.getSelectionModel().getSelectedItem();
        collection.removeDefinition(selected);
        rebuildTable();
    }

    public void editWasPressed(ActionEvent actionEvent) {
        var selected = menuInMenuTable.getSelectionModel().getSelectedItem();
        if(selected == null) return;

        var dialog = new EditMenuInMenuItemDialog(getStage(), selected, tree);
        dialog.getResult().ifPresent(result -> {
            collection.replaceDefinition(selected, result);
            rebuildTable();
            menuInMenuTable.getSelectionModel().select(result);
        });
    }
}
