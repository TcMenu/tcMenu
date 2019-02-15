/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.pluginapi.CodeGenerator;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class DefaultCodeGeneratorRunner implements CodeGeneratorRunner {
    private final System.Logger logger = System.getLogger(DefaultCodeGeneratorRunner.class.getSimpleName());

    private final CurrentEditorProject project;
    private final Map<EmbeddedPlatform, CodeGenerator> codeGenerators;

    public DefaultCodeGeneratorRunner(CurrentEditorProject project, Map<EmbeddedPlatform, CodeGenerator> codeGenerators) {
        this.project = project;
        this.codeGenerators = codeGenerators;
    }

    @Override
    public void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String path,
                                    List<EmbeddedCodeCreator> creators, boolean modal) {
        try {
            logger.log(INFO, "Starting conversion for [" + platform + "] in path [" + path + "]");
            CodeGenerator gen = codeGenerators.get(platform);
            if(gen != null) {
                FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
                BorderPane pane = loader.load();
                CodeGenLoggingController controller = loader.getController();
                controller.init(gen);
                new Thread(() -> {
                    gen.startConversion(Paths.get(path), creators, project.getMenuTree());
                    Platform.runLater(controller::enableCloseButton);
                }).start();
                createDialogStateAndShow(stage, pane, "Code Generator Log", modal);
            }
            else {
                logger.log(ERROR, "Invalid platform detected: " + platform);
            }
        }
        catch(Exception e) {
            logger.log(ERROR, "Unable to create the form", e);
        }
    }
}
