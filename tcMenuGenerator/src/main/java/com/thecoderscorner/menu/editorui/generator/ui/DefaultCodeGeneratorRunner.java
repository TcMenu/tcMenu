package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;

public class DefaultCodeGeneratorRunner implements CodeGeneratorRunner {
    private final Logger logger = LoggerFactory.getLogger(DefaultCodeGeneratorRunner.class);

    private CurrentEditorProject project;
    private ArduinoLibraryInstaller installer;

    public DefaultCodeGeneratorRunner(CurrentEditorProject project, ArduinoLibraryInstaller installer) {
        this.project = project;
        this.installer = installer;
    }

    @Override
    public void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String path, List<EmbeddedCodeCreator> generators) {
        try {
            if(platform == EmbeddedPlatform.ARDUINO) {
                ArduinoGenerator generator = new ArduinoGenerator(Paths.get(path), generators, project.getMenuTree(),
                        new ArduinoSketchFileAdjuster(), installer);
                createLoggerWindow(stage, generator);
            }
            else {
                logger.error("Invalid platform detected");
            }
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
    @SuppressWarnings("Duplicates") // the 4 lines would look worse in their own method..
    private void createLoggerWindow(Stage stage, ArduinoGenerator generator) throws java.io.IOException {
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
