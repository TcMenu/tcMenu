/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AboutController;
import com.thecoderscorner.menu.editorui.controller.EditDialogController;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;

import java.util.Optional;


/** Example of displaying a splash page for a standalone JavaFX application */
public class EditCallbackFunctionDialog extends BaseDialogSupport<EditDialogController> {
    private final String fnDefinition;
    private final boolean runtimeItem;

    public EditCallbackFunctionDialog(Stage stage, boolean modal, String fnDefinition, boolean runtimeItem) {
        this.fnDefinition = fnDefinition;
        this.runtimeItem = runtimeItem;
        tryAndCreateDialog(stage, "/ui/editFunctionCallback.fxml", "Edit Function Callback", modal);
    }

    @Override
    protected void initialiseController(EditDialogController controller) throws Exception {
        controller.initialise(fnDefinition, runtimeItem);
    }

    public Optional<String> getResult() {
        return controller.getResult();
    }
}