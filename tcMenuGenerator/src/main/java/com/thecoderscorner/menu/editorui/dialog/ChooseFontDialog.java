/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ChooseFontController;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import javafx.stage.Stage;

import java.util.Optional;

public class ChooseFontDialog extends BaseDialogSupport<ChooseFontController> {
    private final String currentSel;

    public ChooseFontDialog(Stage stage, String currentText, boolean modal) {
        currentSel = currentText;
        tryAndCreateDialog(stage, "/ui/fontSelectionDialog.fxml", "Choose font", modal);
    }

    public Optional<String> getResultOrEmpty() {
        var result = controller.getResult();
        return result.map(FontDefinition::toString);
    }

    @Override
    protected void initialiseController(ChooseFontController controller) {
        controller.initialise(currentSel);
    }
}
