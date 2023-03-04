/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.NewItemDialog;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.NameAndKey;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class DefaultCodeGeneratorRunner implements CodeGeneratorRunner {
    private final System.Logger logger = System.getLogger(DefaultCodeGeneratorRunner.class.getSimpleName());

    private final CurrentEditorProject project;
    private final EmbeddedPlatforms platforms;
    private final CodePluginManager manager;

    public DefaultCodeGeneratorRunner(CurrentEditorProject project, EmbeddedPlatforms platforms, CodePluginManager mgr) {
        this.project = project;
        this.platforms = platforms;
        this.manager = mgr;
    }

    @Override
    public void startCodeGeneration(Stage stage, EmbeddedPlatform platform, String path,
                                    List<CodePluginItem> creators, List<String> previousPlugins,
                                    boolean modal) {
        try {
            logger.log(INFO, "Starting conversion for [" + platform + "] in path [" + path + "]");
            CodeGenerator gen = platforms.getCodeGeneratorFor(platform, project.getGeneratorOptions());
            if(gen != null) {
                FXMLLoader loader = new FXMLLoader(NewItemDialog.class.getResource("/ui/generatorLog.fxml"));
                loader.setResources(MenuEditorApp.getBundle());
                BorderPane pane = loader.load();
                CodeGenLoggingController controller = loader.getController();
                controller.init(gen);

                // here we get access to objects we need on the other thread, ensuring they are all safe to use on
                // the other thread, then we create the other thread.
                var threadSafeCreators = List.copyOf(creators);
                var threadSafePreviousPluginFiles = allPreviousSourceFiles(previousPlugins);
                var threadSafeMenuTree = project.getMenuTree();
                new Thread(() -> {
                    gen.startConversion(Paths.get(path), threadSafeCreators, threadSafeMenuTree,
                            threadSafePreviousPluginFiles, project.getGeneratorOptions());
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

    private List<String> allPreviousSourceFiles(List<String> previousPlugins) {

        return previousPlugins.stream()
                .map(manager::getPluginById)
                .filter(Optional::isPresent)
                .map(Optional::orElseThrow)
                .flatMap(pl -> pl.getRequiredSourceFiles().stream())
                .map(RequiredSourceFile::getFileName)
                .collect(Collectors.toList());
    }

    private NameAndKey newNameAndKey(CurrentEditorProject project) {
        return new NameAndKey(
                project.getGeneratorOptions().getApplicationUUID().toString(),
                project.getGeneratorOptions().getApplicationName()
        );
    }
}
