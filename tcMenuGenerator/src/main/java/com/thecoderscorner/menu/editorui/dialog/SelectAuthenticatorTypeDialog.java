/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ChooseAuthenticatorController;
import com.thecoderscorner.menu.editorui.controller.EepromTypeSelectionController;
import com.thecoderscorner.menu.editorui.generator.parameters.AuthenticatorDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.EepromDefinition;
import javafx.stage.Stage;

import java.util.Optional;

public class SelectAuthenticatorTypeDialog extends BaseDialogSupport<ChooseAuthenticatorController> {
    private final AuthenticatorDefinition current;

    public SelectAuthenticatorTypeDialog(Stage stage, AuthenticatorDefinition current, boolean modal) {
        this.current = current;
        tryAndCreateDialog(stage, "/ui/authenticationSelectionDialog.fxml", "Choose Authenticator", modal);
    }

    public Optional<AuthenticatorDefinition> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(ChooseAuthenticatorController controller) {
        controller.initialise(current);
    }
}
