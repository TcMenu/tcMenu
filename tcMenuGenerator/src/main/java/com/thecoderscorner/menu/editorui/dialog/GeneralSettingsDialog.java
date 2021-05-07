/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.GeneralSettingsController;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.util.PluginUpgradeTask;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;


/** Example of displaying a splash page for a standalone JavaFX application */
public class GeneralSettingsDialog {
    private final System.Logger logger = System.getLogger(GeneralSettingsDialog.class.getSimpleName());

    public GeneralSettingsDialog(Stage stage, ConfigurationStorage storage, LibraryVersionDetector detector,
                                 ArduinoLibraryInstaller installer, CodePluginManager manager, PluginUpgradeTask upgrader,
                                 String home) {
        try {
            var loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/settingsDialog.fxml"));
            BorderPane pane = loader.load();
            GeneralSettingsController controller = loader.getController();
            controller.initialise(storage, detector, installer, manager, upgrader, home);
            createDialogStateAndShow(stage, pane, "General Application Settings", true);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }
    }
}