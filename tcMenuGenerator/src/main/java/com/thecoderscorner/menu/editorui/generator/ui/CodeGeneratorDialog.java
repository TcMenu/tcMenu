/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The code generator dialog is the starting point for both initiating conversion, and to display the generation options
 */
public class CodeGeneratorDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);

    /**
     * Shows the code generation configuration window, that is always displayed before a conversion starts. It
     * provides the means to edit the generation options, and also to start the generation process.
     * @param stage the place where we display
     * @param editorUI the editorUI object responsible for rendering error alerts
     * @param project the project we are converting
     * @param codeGeneratorRunner the runner that runs the actual conversion
     * @param model true for a modal window
     */
    public void showCodeGenerator(Stage stage, CurrentProjectEditorUI editorUI, CurrentEditorProject project,
                                  CodeGeneratorRunner codeGeneratorRunner, boolean modal) {

        // prevent generation if a filename is not yet set / not saved.
        if(!project.isFileNameSet()) {
            editorUI.alertOnError("Project not yet saved", "Please save the project before attempting generation.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/codeGenerator.fxml"));
            AnchorPane pane = loader.load();
            CodeGeneratorController controller = loader.getController();
            controller.init(project, codeGeneratorRunner);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Code Generator");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            if(modal) {
                dialogStage.showAndWait();
            }
            else {
                dialogStage.show();
            }
        }
        catch(Exception e) {
            logger.error("Unable to create the form", e);
        }
    }
}
