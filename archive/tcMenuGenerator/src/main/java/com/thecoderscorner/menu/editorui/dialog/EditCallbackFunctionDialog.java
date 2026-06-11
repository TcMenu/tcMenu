/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.controller.EditFunctionController;
import javafx.stage.Stage;

import java.util.Optional;


/** Example of displaying a splash page for a standalone JavaFX application */
public class EditCallbackFunctionDialog extends BaseDialogSupport<EditFunctionController> {
    private final String fnDefinition;
    private final MenuItem menuItem;

    public EditCallbackFunctionDialog(Stage stage, boolean modal, String fnDefinition, MenuItem menuItem) {
        this.fnDefinition = fnDefinition;
        this.menuItem = menuItem;
        tryAndCreateDialog(stage, "/ui/editFunctionCallback.fxml", bundle.getString("edit.function.title"), modal);
    }

    @Override
    protected void initialiseController(EditFunctionController controller) throws Exception {
        controller.initialise(fnDefinition, menuItem);
    }

    public Optional<String> getResult() {
        return controller.getResult();
    }
}