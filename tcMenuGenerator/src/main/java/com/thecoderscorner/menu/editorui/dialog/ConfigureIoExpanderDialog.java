/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ChooseFontController;
import com.thecoderscorner.menu.editorui.controller.ConfigureExpanderController;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.Optional;

public class ConfigureIoExpanderDialog extends BaseDialogSupport<ConfigureExpanderController> {
    private final IoExpanderDefinition currentSel;
    private final Collection<String> namesInUse;

    public ConfigureIoExpanderDialog(Stage stage, IoExpanderDefinition def, Collection<String> namesInUse, boolean modal) {
        currentSel = def;
        this.namesInUse = namesInUse;
        tryAndCreateDialog(stage, "/ui/configureIoExpander.fxml", "Configure IO Device", modal);
    }

    public Optional<IoExpanderDefinition> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(ConfigureExpanderController controller) {
        controller.initialise(currentSel, namesInUse);
    }


}
