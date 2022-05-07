package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.generator.parameters.MenuInMenuDefinition;
import com.thecoderscorner.menu.editorui.util.EnumWithStringValue;
import com.thecoderscorner.menu.mgr.MenuInMenu;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.util.EnumWithStringValue.asFriendlyEnum;
import static com.thecoderscorner.menu.editorui.util.EnumWithStringValue.createFriendlyEnum;

public class EditMenuInMenuItemController {

    public Label hostOrPortLabel;
    public Label portOrBaudLabel;
    public TextField nameField;
    public TextField hostField;
    public TextField portBaudField;
    public ComboBox<EnumWithStringValue<MenuInMenuDefinition.ConnectionType>> connectionTypeCombo;
    public Button saveButton;
    public Spinner<Integer> offsetSpinner;
    public Spinner<Integer> maxRangeSpinner;
    public ComboBox<MenuItem> submenuCombo;
    public ComboBox<EnumWithStringValue<MenuInMenu.ReplicationMode>> replicationCombo;
    public Button cancelButton;
    private MenuInMenuDefinition definition;
    private MenuTree tree;

    public void initialise(MenuInMenuDefinition definition, MenuTree tree) {
        this.definition = definition;
        this.tree = tree;
        this.nameField.setText(definition.getVariableName());
        this.hostField.setText(definition.getPortOrIpAddress());
        this.portBaudField.setText(String.valueOf(definition.getPortOrBaud()));
        this.connectionTypeCombo.setItems(createFriendlyEnum(MenuInMenuDefinition.ConnectionType.values()));
        this.connectionTypeCombo.getSelectionModel().select(asFriendlyEnum(definition.getConnectionType()));
        this.connectionTypeCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> connectionTypeChange());
        this.offsetSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, definition.getIdOffset()));
        this.maxRangeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, definition.getMaximumRange()));
        connectionTypeChange();

        submenuCombo.setItems(FXCollections.observableArrayList(tree.getAllSubMenus()));
        submenuCombo.getSelectionModel().select(tree.getMenuById(definition.getSubMenuId()).orElse(MenuTree.ROOT));

        replicationCombo.setItems(createFriendlyEnum(MenuInMenu.ReplicationMode.values()));
        replicationCombo.getSelectionModel().select(asFriendlyEnum(definition.getReplicationMode()));
    }

    private void connectionTypeChange() {
        var ty = connectionTypeCombo.getSelectionModel().getSelectedItem();
        hostOrPortLabel.setText(ty.enumVal() == MenuInMenuDefinition.ConnectionType.SERIAL ? "Port" : "IP Address");
        portOrBaudLabel.setText(ty.enumVal() == MenuInMenuDefinition.ConnectionType.SERIAL ? "Baud" : "Port");
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
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.showAndWait();
            return;
        }

        try {

            definition = new MenuInMenuDefinition(
                    nameField.getText(),
                    hostField.getText(),
                    Integer.parseInt(portBaudField.getText()),
                    connectionTypeCombo.getSelectionModel().getSelectedItem().enumVal(),
                    replicationCombo.getSelectionModel().getSelectedItem().enumVal(),
                    submenuCombo.getSelectionModel().getSelectedItem().getId(),
                    offsetSpinner.getValue(),
                    maxRangeSpinner.getValue()
            );
            closeIt();
        }
        catch (Exception e) {
            var alert = new Alert(Alert.AlertType.ERROR, "Unable to process the fields provided");
            alert.setHeaderText("Please check the values provided");
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.showAndWait();

        }
    }

    public Optional<MenuInMenuDefinition> getResult() {
        return definition == null ? Optional.empty() : Optional.of(definition);
    }
}
