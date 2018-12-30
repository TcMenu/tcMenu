package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class DefaultCodeGeneratorRunner implements CodeGeneratorRunner {
    private final Logger logger = LoggerFactory.getLogger(DefaultCodeGeneratorRunner.class);

    private final CurrentEditorProject project;
    private final Map<EmbeddedPlatform, CodeGenerator> codeGenerators;

    public DefaultCodeGeneratorRunner(CurrentEditorProject project, Map<EmbeddedPlatform, CodeGenerator> codeGenerators) {
        this.project = project;
        this.codeGenerators = codeGenerators;
    }

    @Override
    public void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String path, List<EmbeddedCodeCreator> creators) {
        try {
            logger.info("Starting conversion for [" + platform + "] in path [" + path + "]");
            CodeGenerator gen = codeGenerators.get(platform);
            if(gen != null) {
                startGeneratorWithLoggerWindow(stage, path, creators, gen);
            }
            else {
                logger.error("Invalid platform detected: " + platform);
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
    private void startGeneratorWithLoggerWindow(Stage stage, String path, List<EmbeddedCodeCreator> creators,
                                                CodeGenerator generator) throws java.io.IOException {
        FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
        BorderPane pane = loader.load();
        CodeGenLoggingController controller = loader.getController();
        controller.init(generator);
        new Thread(() -> {
            generator.startConversion(Paths.get(path), creators, project.getMenuTree());
            Platform.runLater(controller::enableCloseButton);
        }).start();
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Code Generator Log");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);
        Scene scene = new Scene(pane);
        dialogStage.setScene(scene);
        dialogStage.showAndWait();
    }
}
