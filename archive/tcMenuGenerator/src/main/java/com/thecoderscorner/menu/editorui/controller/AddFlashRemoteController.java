package com.thecoderscorner.menu.editorui.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.FlashRemoteId;
import static com.thecoderscorner.menu.editorui.util.StringHelper.isStringEmptyOrNull;


public class AddFlashRemoteController {
    public Button addRemoteButton;
    public TextField uuidField;
    public TextField nameField;

    private Optional<FlashRemoteId> result = Optional.empty();

    public void initialise(Optional<FlashRemoteId> remoteId) {
        nameField.textProperty().addListener((observableValue, s, t1) -> textChanged());
        uuidField.textProperty().addListener((observableValue, s, t1) -> textChanged());
        remoteId.ifPresent(id -> {
            nameField.setText(id.name());
            uuidField.setText(id.uuid());
        });
    }

    private void textChanged() {
        addRemoteButton.setDisable(isStringEmptyOrNull(nameField.getText()) || isStringEmptyOrNull(uuidField.getText()));
    }

    public Optional<FlashRemoteId> getResult() {
        return result;
    }

    public void onCancel(ActionEvent actionEvent) {
        result = Optional.empty();
        ((Stage)uuidField.getScene().getWindow()).close();
    }

    public void onAddRemote(ActionEvent actionEvent) {
        result = Optional.of(new FlashRemoteId(nameField.getText(), uuidField.getText()));
        ((Stage)uuidField.getScene().getWindow()).close();
    }
}
