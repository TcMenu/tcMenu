package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.UUID;

/**
 * This is the controller for `addAuthEntry.fxml`. It allows the user to enter an app name and UUID pair and will
 * validate the data entered. If successful the chosenName and chosenUuid will be populated on close. The can be
 * accessed using `getUserName()` and `getUuid()`.
 */
public class AddAuthEntryController {
    public TextField appNameField;
    public TextField uuidField;
    public Button addAuthenticationButton;
    private String chosenName;
    private UUID chosenUuid;

    public void cancelWasPressed(ActionEvent actionEvent) {
        chosenName = null;
        chosenUuid = null;
        ((Stage)uuidField.getScene().getWindow()).close();
    }

    public void addAuthenticationWasPressed(ActionEvent actionEvent) {
        if(appNameField.getText().isEmpty() || uuidField.getText().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"You must enter both values", ButtonType.CLOSE);
            alert.getDialogPane().setStyle("-fx-font-size:" + GlobalSettings.defaultFontSize());
            alert.showAndWait();
            return;
        }
        chosenName = appNameField.getText();
        try {
            chosenUuid = UUID.fromString(uuidField.getText());
            ((Stage)uuidField.getScene().getWindow()).close();
        }
        catch(Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR,"Please enter a valid UUID", ButtonType.CLOSE);
            alert.getDialogPane().setStyle("-fx-font-size:" + GlobalSettings.defaultFontSize());
            alert.showAndWait();
        }
    }

    public String getUserName() {
        return chosenName;
    }

    public UUID getUuid() {
        return chosenUuid;
    }
}
