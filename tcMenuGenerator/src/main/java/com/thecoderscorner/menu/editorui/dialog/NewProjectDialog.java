/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.NewProjectController;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;

public class NewProjectDialog extends BaseDialogSupport<NewProjectController> {
    private final CurrentEditorProject project;
    private final ConfigurationStorage storage;
    private final EmbeddedPlatforms platforms;

    public NewProjectDialog(Stage stage, ConfigurationStorage storage, EmbeddedPlatforms platforms,
                                            CurrentEditorProject project, boolean modal) {
        this.storage = storage;
        this.platforms = platforms;
        this.project = project;

        tryAndCreateDialog(stage, "/ui/createNewProject.fxml", "Create New Project", modal);
    }

    @Override
    protected void initialiseController(NewProjectController controller) {
        controller.initialise(storage, platforms, project);
    }
}
