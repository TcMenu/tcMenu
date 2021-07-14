package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.EepromAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.NoAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.eeprom.*;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.AUTHENTICATOR_HELP_PAGE;
import static com.thecoderscorner.menu.editorui.dialog.AppInformationPanel.EEPROM_HELP_PAGE;
import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.*;

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

        }

        main.selectedToggleProperty().addListener((v, n, o) -> enableTheRightItems());
        pinFlashField.textProperty().addListener((v, n, o) -> enableTheRightItems());
        eepromStartField.textProperty().addListener((v, n, o) -> enableTheRightItems());

        enableTheRightItems();
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
}
