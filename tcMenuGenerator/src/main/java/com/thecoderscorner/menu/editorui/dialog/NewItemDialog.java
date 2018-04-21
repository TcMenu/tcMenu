package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.NewItemController;
import com.thecoderscorner.menu.editorui.project.MenuIdChooserImpl;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class NewItemDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);

    public static Optional<MenuItem> showNewItemRequest(Stage stage, MenuTree tree) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/newItemDialog.fxml"));
            BorderPane pane = loader.load();
            NewItemController controller = loader.getController();
            controller.initialise(new MenuIdChooserImpl(tree));

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Create new item");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            return controller.getResult();
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating form", ButtonType.CLOSE);
            alert.setHeaderText("Error creating the form, more detail is in the log");
            alert.showAndWait();

            logger.error("Unable to create the form", e);
        }
        return Optional.empty();
    }
}
