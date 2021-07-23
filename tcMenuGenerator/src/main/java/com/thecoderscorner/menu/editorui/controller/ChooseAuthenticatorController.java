package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.AddFlashRemoteDialog;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.AUTHENTICATOR_HELP_PAGE;
import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.FlashRemoteId;

public class ChooseAuthenticatorController {
    public Button okButton;
    public Label eepromStartLabel;
    public TextField eepromStartField;
    public Label pinFlashLabel;
    public Label variableFlashLabel;
    public TextField pinFlashField;
    public ToggleGroup main;
    public RadioButton noAuthRadio;
    public RadioButton eepromAuthRadio;
    public RadioButton flashAuthRadio;
    public TextField eepromNumRemotes;
    public Label eepromNumRemLabel;
    public ListView<FlashRemoteId> flashVarList;
    public Button addButton;
    public Button removeButton;

    private Optional<AuthenticatorDefinition> result = Optional.empty();

    public void initialise(AuthenticatorDefinition authType) {
        if(authType instanceof NoAuthenticatorDefinition) {
            noAuthRadio.setSelected(true);
        }
        else if(authType instanceof EepromAuthenticatorDefinition romAuth) {
            eepromAuthRadio.setSelected(true);
            eepromStartField.setText(Integer.toString(romAuth.getOffset()));
        }
        else if(authType instanceof ReadOnlyAuthenticatorDefinition flashAuth) {
            flashAuthRadio.setSelected(true);
            pinFlashField.setText(flashAuth.getPin());
            flashVarList.getItems().addAll(flashAuth.getRemoteIds());
        }

        main.selectedToggleProperty().addListener((v, n, o) -> enableTheRightItems());
        pinFlashField.textProperty().addListener((v, n, o) -> enableTheRightItems());
        eepromStartField.textProperty().addListener((v, n, o) -> enableTheRightItems());

        enableTheRightItems();

        flashVarList.getSelectionModel().selectedItemProperty().addListener((observableValue, newVal, oldVal) ->
                removeButton.setDisable(newVal == null));

        if(flashVarList.getItems().size() > 0) {
            flashVarList.getSelectionModel().select(0);
        }

        flashVarList.setOnMouseClicked(click -> {

            if (click.getClickCount() == 2) {
                //Use ListView's getSelected Item
                var itemSel = flashVarList.getSelectionModel().getSelectedItem();
                var itemIdx = flashVarList.getSelectionModel().getSelectedIndex();
                if(itemIdx == -1 || itemSel == null) return;

                AddFlashRemoteDialog dlg = new AddFlashRemoteDialog((Stage)okButton.getScene().getWindow(), Optional.of(itemSel), true);
                var res = dlg.getResultOrEmpty();
                res.ifPresent(editedRemote -> {
                    flashVarList.getItems().set(itemIdx, editedRemote);
                });
            }
        });
    }

    private void enableTheRightItems() {
        var okEnabled = false;
        var eepromSelected = eepromAuthRadio.isSelected();
        var flashSelected = flashAuthRadio.isSelected();

        if(eepromSelected) {
            if(StringHelper.isStringEmptyOrNull(eepromNumRemotes.getText())) {
                eepromNumRemotes.setText("6");
            }
            okEnabled = eepromStartField.getText().matches("^[0-9]+$") && eepromNumRemotes.getText().matches("^[1-9]$");
            okEnabled = okEnabled && (Integer.parseInt(eepromNumRemotes.getText()) <= 9);
        }
        else if(flashSelected) {
            okEnabled = flashVarList.getItems().size() > 0 && !StringHelper.isStringEmptyOrNull(pinFlashField.getText());
        }
        else {
            okEnabled = true;
        }
        okButton.setDisable(!okEnabled);

        eepromNumRemotes.setDisable(!eepromSelected);
        eepromNumRemLabel.setDisable(!eepromSelected);
        eepromStartField.setDisable(!eepromSelected);
        eepromStartLabel.setDisable(!eepromSelected);

        addButton.setDisable(!flashSelected);
        removeButton.setDisable(!flashSelected);
        flashVarList.setDisable(!flashSelected);
        variableFlashLabel.setDisable(!flashSelected);
        pinFlashField.setDisable(!flashSelected);
        pinFlashLabel.setDisable(!flashSelected);
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        ((Stage)okButton.getScene().getWindow()).close();
    }

    public void onCreatePressed(ActionEvent actionEvent) {
        if(eepromAuthRadio.isSelected()) {
            result = Optional.of(new EepromAuthenticatorDefinition(
                    Integer.parseInt(eepromStartField.getText()),
                    Integer.parseInt(eepromNumRemotes.getText())
            ));
        }
        else if(flashAuthRadio.isSelected()) {
            result = Optional.of(new ReadOnlyAuthenticatorDefinition(pinFlashField.getText(), flashVarList.getItems()));
        }
        else if(noAuthRadio.isSelected()) {
            result = Optional.of(new NoAuthenticatorDefinition());
        }

        ((Stage)okButton.getScene().getWindow()).close();
    }

    public Optional<AuthenticatorDefinition> getResult() {
        return result;
    }

    public void onHelpPressed(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(AUTHENTICATOR_HELP_PAGE);
    }

    public void onFlashAddRemote(ActionEvent actionEvent) {
        AddFlashRemoteDialog dlg = new AddFlashRemoteDialog((Stage)okButton.getScene().getWindow(), Optional.empty(), true);
        var res = dlg.getResultOrEmpty();
        res.ifPresent(newRemote -> {
            flashVarList.getItems().add(newRemote);
            flashVarList.getSelectionModel().select(newRemote);
            removeButton.setDisable(false);
            okButton.setDisable(false);
        });
    }

    public void onFlashRemoveRemote(ActionEvent actionEvent) {
        var sel = flashVarList.getSelectionModel().getSelectedItem();
        if(sel != null) {
            flashVarList.getItems().remove(sel);
            okButton.setDisable(flashVarList.getItems().isEmpty());
        }
    }
}
