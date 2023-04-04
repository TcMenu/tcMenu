/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.EepromTypeSelectionController;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import javafx.stage.Stage;

import java.util.Optional;

public class SelectEepromTypeDialog extends BaseDialogSupport<EepromTypeSelectionController> {
    private final EepromDefinition current;

    public SelectEepromTypeDialog(Stage stage, EepromDefinition current, boolean modal) {
        this.current = current;
        tryAndCreateDialog(stage, "/ui/eepromTypeSelectionDialog.fxml", bundle.getString("eeprom.type.title"), modal);
    }

    public Optional<EepromDefinition> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(EepromTypeSelectionController controller) {
        controller.initialise(current);
    }
}
