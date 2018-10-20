/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
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

/**
 * The code generator dialog is the starting point for both initiating conversion, and to display the generation options
 */
public class CodeGeneratorDialog {
    private static final Logger logger = LoggerFactory.getLogger(NewItemDialog.class);
    private Stage stage;
    private CurrentEditorProject project;
    private ArduinoLibraryInstaller installer;

    /**
     * Shows the code generation configuration window, that is always displayed before a conversion starts. It
     * provides the means to edit the generation options, and also to start the generation process.
     * @param stage the place where we display
     * @param project the project we are converting.
     * @param installer an installer object, which knows where to find Arduino library files.
     */
    public void showCodeGenerator(Stage stage, CurrentEditorProject project, ArduinoLibraryInstaller installer) {
        this.stage = stage;
        this.project = project;
        this.installer = installer;
        try {
            FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/codeGenerator.fxml"));
            AnchorPane pane = loader.load();
            CodeGeneratorController controller = loader.getController();
            controller.init(project, this);
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

    /**
     * Start the Arduino conversion, which is presently the only supported conversion.
     * @param dir the directory in which to create the files.
     * @param generators a list of prebuilt generators that have been configured.
     */
    public void startArduinoGenerator(String dir, List<EmbeddedCodeCreator> generators) {
        try {
            ArduinoGenerator generator = new ArduinoGenerator(Paths.get(dir), generators, project.getMenuTree(),
                    new ArduinoSketchFileAdjuster(), installer);
            createLoggerWindow(generator);
        }
        catch(Exception e) {
            logger.error("Unable to create the form", e);
        }
    }

    /**
     * Make a standard logger window that will contain the logged result of a code generation run.
     * @param generator the generator that will be used to do the conversion.
     * @throws java.io.IOException if it all goes wrong.
     */
    private void createLoggerWindow(ArduinoGenerator generator) throws java.io.IOException {
        FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
        BorderPane pane = loader.load();
        CodeGenLoggingController controller = loader.getController();
        controller.init(generator);
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Code Generator Log");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);
        Scene scene = new Scene(pane);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
