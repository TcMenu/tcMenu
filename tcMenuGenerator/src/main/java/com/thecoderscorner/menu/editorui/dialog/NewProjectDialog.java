/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.NewProjectController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;

public class NewProjectDialog extends BaseDialogSupport<NewProjectController> {
    private final CurrentEditorProject project;
    private final ConfigurationStorage storage;
    private final EmbeddedPlatforms platforms;
    private final ProjectPersistor projectPersistor;
    private final CodeGeneratorSupplier codeGenSupplier;

    public NewProjectDialog(Stage stage, ConfigurationStorage storage, EmbeddedPlatforms platforms,
                            CurrentEditorProject project, CodeGeneratorSupplier codeGenSupplier,
                            ProjectPersistor persistor, boolean modal) {
        this.storage = storage;
        this.project = project;
        this.projectPersistor = persistor;
        this.codeGenSupplier = codeGenSupplier;
        this.platforms = platforms;

        tryAndCreateDialog(stage, "/ui/createNewProject.fxml", bundle.getString("create.project.title"), modal);
    }

    @Override
    protected void initialiseController(NewProjectController controller) {
        controller.initialise(storage, project, codeGenSupplier, projectPersistor, platforms);
    }
}
