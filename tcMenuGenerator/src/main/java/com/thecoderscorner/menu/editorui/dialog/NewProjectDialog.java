/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.NewProjectController;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShow;
import static java.lang.System.Logger.Level.ERROR;

public class NewProjectDialog {
    private static final System.Logger logger = System.getLogger(NewProjectDialog.class.getSimpleName());

    public NewProjectDialog(Stage stage, ConfigurationStorage storage, EmbeddedPlatforms platforms,
                                            CurrentEditorProject project) {
        try {
            FXMLLoader loader = new FXMLLoader(NewProjectDialog.class.getResource("/ui/createNewProject.fxml"));
            BorderPane pane = loader.load();
            NewProjectController controller = loader.getController();
            controller.initialise(storage, platforms, project);
            createDialogStateAndShow(stage, pane, "Create New Project", true);
        }
        catch(Exception e) {
            // in this case, just get out of here.
            logger.log(ERROR, "Unable to create the form", e);
        }
    }

}
