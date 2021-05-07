/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.SplashScreenController;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;


/** Example of displaying a splash page for a standalone JavaFX application */
public class SplashScreenDialog {
    private final System.Logger logger = System.getLogger(SplashScreenDialog.class.getSimpleName());

    private SplashScreenController controller;
    private Stage dialogStage;

    public SplashScreenDialog(Stage stage, CurrentProjectEditorUI editorUI, boolean modal) {
        try {
            var loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/splashScreen.fxml"));
            GridPane pane = loader.load();
            controller = loader.getController();
            controller.initialise(editorUI);
            createDialogStateAndShow(stage, pane, "TcMenu Designer", modal);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }
    }
}