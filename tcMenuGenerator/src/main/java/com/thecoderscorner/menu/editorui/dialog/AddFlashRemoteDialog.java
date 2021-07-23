/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AddFlashRemoteController;
import com.thecoderscorner.menu.editorui.controller.ChooseFontController;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.*;

public class AddFlashRemoteDialog extends BaseDialogSupport<AddFlashRemoteController> {
    private final Optional<FlashRemoteId> currentSel;

    public AddFlashRemoteDialog(Stage stage, Optional<FlashRemoteId> flashRemoteId, boolean modal) {
        currentSel = flashRemoteId;
        var textMode = flashRemoteId.isEmpty() ? "New" : "Edit";
        tryAndCreateDialog(stage, "/ui/addNewFlashRemote.fxml", textMode + " Flash Remote", modal);
    }

    public Optional<FlashRemoteId> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(AddFlashRemoteController controller) {
        controller.initialise(currentSel);
    }
}
