/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ChooseIoExpanderController;
import com.thecoderscorner.menu.editorui.controller.ConfigureExpanderController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.stage.Stage;

import java.util.Optional;

public class ChooseIoExpanderDialog extends BaseDialogSupport<ChooseIoExpanderController> {
    private final Optional<IoExpanderDefinition> currentSel;
    private final CurrentEditorProject project;

    public ChooseIoExpanderDialog(Stage stage, Optional<IoExpanderDefinition> def, CurrentEditorProject project, boolean modal) {
        this.currentSel = def;
        this.project = project;
        tryAndCreateDialog(stage, "/ui/chooseIoExpander.fxml", "Configured IO Devices", modal);
    }

    public Optional<String> getResultOrEmpty() {
        var result = controller.getResult();
        return result.map(IoExpanderDefinition::getId);
    }

    @Override
    protected void initialiseController(ChooseIoExpanderController controller) {
        controller.initialise(currentSel, project);
    }
}
