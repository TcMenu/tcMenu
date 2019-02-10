/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.NewItemController;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;

public class NewItemDialog {
    private static final System.Logger logger = System.getLogger(NewItemDialog.class.getSimpleName());
    private NewItemController controller;
    private Stage dialogStage;

    public NewItemDialog(Stage stage, MenuTree tree, CurrentProjectEditorUI editorUI) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/newItemDialog.fxml"));
            BorderPane pane = loader.load();
            controller = loader.getController();
            controller.initialise(new MenuIdChooserImpl(tree), editorUI);

            dialogStage = new Stage();
            dialogStage.setTitle("Create new item");
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

    public Optional<MenuItem> showAndWait() {
        dialogStage.showAndWait();
        return controller.getResult();
    }

    public void show() {
        dialogStage.show();
    }

    public Optional<MenuItem> getResultOrEmpty() {
        return controller.getResult();
    }
}
