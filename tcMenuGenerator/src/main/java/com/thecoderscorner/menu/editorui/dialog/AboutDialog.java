/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AboutController;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import static java.lang.System.Logger.Level.ERROR;


/** Example of displaying a splash page for a standalone JavaFX application */
public class AboutDialog {
    private final System.Logger logger = System.getLogger(AboutDialog.class.getSimpleName());

    private AboutController controller;
    private Stage dialogStage;

    public AboutDialog(Stage stage, ArduinoLibraryInstaller installer) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/aboutDialog.fxml"));
            BorderPane pane = loader.load();
            controller = loader.getController();
            controller.initialise(installer);

            dialogStage = new Stage();
            dialogStage.setTitle("About tcMenu Designer");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }
    }

    public void showAndWait() {
        dialogStage.showAndWait();
    }

    public void show() {
        dialogStage.show();
    }
}