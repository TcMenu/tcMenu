/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.RegistrationController;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static java.lang.System.Logger.Level.ERROR;

public class RegistrationDialog extends BaseDialogSupport<RegistrationController> {

    private ConfigurationStorage storage;
    private String registerUrl;

    public RegistrationDialog(ConfigurationStorage storage, Stage stage, String registerUrl) {
        this.storage = storage;
        this.registerUrl = registerUrl;
        tryAndCreateDialog(stage, "/ui/registrationDialog.fxml", "Please Register with us", true);
    }

    @Override
    protected void initialiseController(RegistrationController controller) {
        controller.init(storage, registerUrl);
    }
}
