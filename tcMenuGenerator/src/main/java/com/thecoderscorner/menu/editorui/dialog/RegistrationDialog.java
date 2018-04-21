package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.RegistrationController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegistrationDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);

    public static void showRegistration(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/registrationDialog.fxml"));
            BorderPane pane = loader.load();
            RegistrationController controller = loader.getController();
            controller.init();
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Please Register with us");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        }
        catch(Exception e) {
            // in this case, just get out of here.
            logger.error("Unable to create the form", e);
        }
    }

}
