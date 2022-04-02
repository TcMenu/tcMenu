package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

import java.awt.*;
import java.util.Optional;
import java.util.UUID;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.asFxColor;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.fromFxColor;

public class GeneralSettingsController {
    public TextField appNameField;
    public TextField appUuidField;
    public CheckBox enableLayoutCustomizationCheck;
    private GlobalSettings settings;

    public void initialise(GlobalSettings settings) {
        this.settings = settings;
        appNameField.setText(settings.getAppName());
        appUuidField.setText(settings.getAppUuid());
        enableLayoutCustomizationCheck.setSelected(settings.isSetupLayoutModeEnabled());
    }


    public void onChangeUUID(ActionEvent actionEvent) {
        var alert = new Alert(Alert.AlertType.CONFIRMATION, "Really change UUID, all saved authentications will be lost?", ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> buttonType = alert.showAndWait();
        if(buttonType.isPresent() && buttonType.get() == ButtonType.YES) {
            appUuidField.setText(UUID.randomUUID().toString());
        }
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        settings.setAppName(appNameField.getText());
        settings.setAppUuid(appUuidField.getText());
        settings.save();
    }

    public void onLayoutCustomizableChange(ActionEvent actionEvent) {
        settings.setSetupLayoutModeEnabled(enableLayoutCustomizationCheck.isSelected());
    }
}
