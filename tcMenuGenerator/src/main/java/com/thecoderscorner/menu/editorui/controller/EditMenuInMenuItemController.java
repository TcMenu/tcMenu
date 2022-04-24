package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.SubMenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

public class EditMenuInMenuItemController {

    public Label hostOrPortLabel;
    public Label portOrBaudLabel;
    public TextField nameField;
    public TextField hostField;
    public TextField portBaudField;
    public ComboBox<MenuInMenuDefinition.ConnectionType> connectionTypeCombo;
    public Button saveButton;
    public Spinner<Integer> offsetSpinner;
    public Spinner<Integer> maxRangeSpinner;
    public ComboBox<MenuItem> submenuCombo;
    private MenuInMenuDefinition definition;
    private MenuTree tree;

    public void initialise(MenuInMenuDefinition definition, MenuTree tree) {
        this.definition = definition;
        this.tree = tree;
        this.nameField.setText(definition.variableName());
        this.hostField.setText(definition.portOrIpAddress());
        this.portBaudField.setText(String.valueOf(definition.portOrBaud()));
        this.portBaudField.setText(String.valueOf(definition.portOrBaud()));
        this.connectionTypeCombo.setItems(FXCollections.observableArrayList(MenuInMenuDefinition.ConnectionType.values()));
        this.connectionTypeCombo.getSelectionModel().select(definition.connectionType());
        this.connectionTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> connectionTypeChange());
        this.offsetSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, definition.idOffset()));
        this.maxRangeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, definition.maximumRange()));
        connectionTypeChange();

        submenuCombo.setItems(FXCollections.observableArrayList(tree.getAllSubMenus()));
        submenuCombo.getSelectionModel().select(tree.getMenuById(definition.subMenuId()).orElse(MenuTree.ROOT));
    }

    private void connectionTypeChange() {
        var ty = connectionTypeCombo.getSelectionModel().getSelectedItem();
        hostOrPortLabel.setText(ty == MenuInMenuDefinition.ConnectionType.SERIAL ? "Port" : "IP Address");
        portOrBaudLabel.setText(ty == MenuInMenuDefinition.ConnectionType.SERIAL ? "Baud" : "Port");
    }

    public void closeWasPressed(ActionEvent actionEvent) {
        definition = null;
        closeIt();
    }

    private void closeIt() {
        ((Stage)saveButton.getScene().getWindow()).close();
    }

    public void saveWasPressed(ActionEvent actionEvent) {
        if(nameField.getText().isEmpty() || hostField.getText().isEmpty()) {
            var alert = new Alert(Alert.AlertType.ERROR, "Name and connection details must be provided");
            alert.setHeaderText("Missing fields must be corrected");
            alert.showAndWait();
            return;
        }

        try {

            definition = new MenuInMenuDefinition(
                    nameField.getText(),
                    hostField.getText(),
                    Integer.parseInt(portBaudField.getText()),
                    connectionTypeCombo.getSelectionModel().getSelectedItem(),
                    submenuCombo.getSelectionModel().getSelectedItem().getId(),
                    offsetSpinner.getValue(),
                    maxRangeSpinner.getValue()
            );
            closeIt();
        }
        catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Unable to process the fields provided");
            alert.setHeaderText("Please check the values provided");
            alert.showAndWait();

        }
    }

    public Optional<MenuInMenuDefinition> getResult() {
        return definition == null ? Optional.empty() : Optional.of(definition);
    }
}
