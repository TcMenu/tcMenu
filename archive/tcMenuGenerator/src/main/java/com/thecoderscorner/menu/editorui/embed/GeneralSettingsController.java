package com.thecoderscorner.menu.editorui.embed;

import com.thecoderscorner.embedcontrol.core.service.AppDataStore;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcPreferencesPersistence;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.UUID;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;

public class GeneralSettingsController {
    public TextField appNameField;
    public TextField appUuidField;
    public Spinner<Integer> fontSizeSpinner;
    public CheckBox showSubRecursive;
    private GlobalSettings settings;
    private AppDataStore dataStore;

    public void initialise(GlobalSettings settings, AppDataStore dataStore) {
        this.settings = settings;
        this.dataStore = dataStore;
        appNameField.setText(settings.getAppName());
        appUuidField.setText(settings.getAppUuid());
        fontSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 100, settings.getDefaultFontSize()));
        showSubRecursive.setSelected(settings.isDefaultRecursiveRendering());
    }

    public void onChangeUUID(ActionEvent actionEvent) {
        var btn = showAlertAndWait(Alert.AlertType.CONFIRMATION, "Really change UUID, all saved authentications will be lost?", ButtonType.YES, ButtonType.NO);
        if(btn.orElse(ButtonType.NO) == ButtonType.YES) {
            appUuidField.setText(UUID.randomUUID().toString());
        }
    }

    public void onSaveChanges(ActionEvent actionEvent) {
        settings.setAppName(appNameField.getText());
        settings.setAppUuid(appUuidField.getText());
        settings.setDefaultRecursiveRendering(showSubRecursive.isSelected());
        settings.setDefaultFontSize(fontSizeSpinner.getValue());
        try {
            dataStore.updateGlobalSettings(new TcPreferencesPersistence(settings));
        } catch (DataException e) {
            showAlertAndWait(Alert.AlertType.ERROR, "Save Changes to configuration", "Saving failed " + e.getMessage(), ButtonType.CLOSE);
        }
    }

    public void onLayoutCustomizableChange(ActionEvent actionEvent) {
    }
}
