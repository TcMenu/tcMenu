/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.pluginapi.CodeGenerator;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.NameAndKey;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.List;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class DefaultCodeGeneratorRunner implements CodeGeneratorRunner {
    private final System.Logger logger = System.getLogger(DefaultCodeGeneratorRunner.class.getSimpleName());

    private final CurrentEditorProject project;
    private EmbeddedPlatforms platforms;

    public DefaultCodeGeneratorRunner(CurrentEditorProject project, EmbeddedPlatforms platforms) {
        this.project = project;
        this.platforms = platforms;
    }

    @Override
    public void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String path,
                                    List<EmbeddedCodeCreator> creators, List<String> previousPlugins,
                                    boolean modal) {
        try {
            logger.log(INFO, "Starting conversion for [" + platform + "] in path [" + path + "]");
            CodeGenerator gen = platforms.getCodeGeneratorFor(platform, project.getGeneratorOptions());
            if(gen != null) {
                FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
                BorderPane pane = loader.load();
                CodeGenLoggingController controller = loader.getController();
                controller.init(gen);
                new Thread(() -> {
                    gen.startConversion(Paths.get(path), creators, project.getMenuTree(), newNameAndKey(project), previousPlugins);
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

    private NameAndKey newNameAndKey(CurrentEditorProject project) {
        return new NameAndKey(
                project.getGeneratorOptions().getApplicationUUID().toString(),
                project.getGeneratorOptions().getApplicationName()
        );
    }
}
