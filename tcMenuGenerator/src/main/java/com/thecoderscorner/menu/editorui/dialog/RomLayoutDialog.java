package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.RomLayoutController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RomLayoutDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);

    public static void showLayoutDialog(Stage stage, MenuTree menuTree) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/romLayoutDialog.fxml"));
            BorderPane pane = loader.load();
            RomLayoutController controller = loader.getController();
            controller.init(menuTree);

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Rom layout");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.error("Unable to create the form", e);
        }

    }
}
