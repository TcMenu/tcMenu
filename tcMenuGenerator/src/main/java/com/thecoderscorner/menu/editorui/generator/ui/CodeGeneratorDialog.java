/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;

public class CodeGeneratorDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);

    public static void showCodeGenerator(Stage stage, CurrentEditorProject project) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/codeGenerator.fxml"));
            AnchorPane pane = loader.load();
            CodeGeneratorController controller = loader.getController();
            controller.init(project);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Code Generator");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        }
        catch(Exception e) {
            logger.error("Unable to create the form", e);
        }
    }

    public static void showCodeLoggerWindow(Stage stage, String dir, List<EmbeddedCodeCreator> generators, MenuTree tree) {
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
            BorderPane pane = loader.load();
            CodeGenLoggingController controller = loader.getController();
            controller.init(Paths.get(dir), generators, tree);
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Code Generator Log");
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.initOwner(stage);
            Scene scene = new Scene(pane);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
        }
        catch(Exception e) {
            logger.error("Unable to create the form", e);
        }
    }
}
