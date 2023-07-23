package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.DatabaseAppDataStore;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.TextField;

import java.util.Optional;
import java.util.UUID;

public class GeneralSettingsController {
    public TextField appNameField;
    public TextField appUuidField;
    private GlobalSettings settings;
    private AppDataStore dataStore;

    public void initialise(GlobalSettings settings, AppDataStore dataStore) {
        this.settings = settings;
        this.dataStore = dataStore;
        appNameField.setText(settings.getAppName());
        appUuidField.setText(settings.getAppUuid());
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
        dataStore.updateGlobalSettings(settings);
    }

    public void onLayoutCustomizableChange(ActionEvent actionEvent) {
    }
}
