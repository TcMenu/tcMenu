/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.RegistrationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;

public class RegistrationDialog {
    private static final System.Logger logger = System.getLogger(NewItemDialog.class.getSimpleName());

    public static void showRegistration(ConfigurationStorage storage, Stage stage, String registerUrl) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/registrationDialog.fxml"));
            BorderPane pane = loader.load();
            RegistrationController controller = loader.getController();
            controller.init(storage, registerUrl);
            createDialogStateAndShow(stage, pane, "Please Register with us", true);
        }
        catch(Exception e) {
            // in this case, just get out of here.
            logger.log(ERROR, "Unable to create the form", e);
        }
    }

}
