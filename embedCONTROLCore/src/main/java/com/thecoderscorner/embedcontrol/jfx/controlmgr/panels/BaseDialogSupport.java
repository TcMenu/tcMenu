package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;

public abstract class BaseDialogSupport {
    private static final System.Logger logger = System.getLogger(BaseDialogSupport.class.getSimpleName());

    /**
     * Usually called to initialise the dialog and present on screen.
     * @param stage the stage to present on
     * @param resource the FXML resource file name as a string.
     * @param title the title for the dialog
     * @param modal if it should be modal.
     */
     public static <T> void tryAndCreateDialog(Stage stage, String resource, String title, boolean modal, Consumer<T> controllerInitializer) {
        try {
            var loader = new FXMLLoader(BaseDialogSupport.class.getResource(resource));
            Pane pane = loader.load();
            T controller = loader.getController();
            controllerInitializer.accept(controller);
            createDialogStateAndShow(stage, pane, title, modal);
        }
        catch(Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating " + title, ButtonType.CLOSE);
            alert.setHeaderText("Unexpected error during creation of dialog, more detail is in the log");
            alert.showAndWait();

            logger.log(ERROR, "Unable to create the form", e);
        }
    }

    public static void createDialogStateAndShow(Stage parent, Pane root, String title, boolean modal) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle(title);
        dialogStage.initOwner(parent);

        Scene scene = new Scene(root);

        dialogStage.setScene(scene);
        if (modal) {
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.showAndWait();
        }
        else {
            dialogStage.show();
        }
    }
}
