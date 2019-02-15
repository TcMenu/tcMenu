/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.RomLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;

public class RomLayoutDialog {
    private static final System.Logger logger = System.getLogger(NewItemDialog.class.getSimpleName());
    private Stage dialogStage;

    public RomLayoutDialog(Stage stage, MenuTree menuTree, boolean modal) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/romLayoutDialog.fxml"));
            BorderPane pane = loader.load();
            RomLayoutController controller = loader.getController();
            controller.init(menuTree);

            createDialogStateAndShow(stage, pane, "Rom Layout", modal);
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }
    }
}
