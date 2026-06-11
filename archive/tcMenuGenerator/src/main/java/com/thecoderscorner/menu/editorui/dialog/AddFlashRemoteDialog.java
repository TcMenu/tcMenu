/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AddFlashRemoteController;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.parameters.auth.ReadOnlyAuthenticatorDefinition.FlashRemoteId;

public class AddFlashRemoteDialog extends BaseDialogSupport<AddFlashRemoteController> {
    private final Optional<FlashRemoteId> currentSel;

    public AddFlashRemoteDialog(Stage stage, Optional<FlashRemoteId> flashRemoteId, boolean modal) {
        currentSel = flashRemoteId;
        String title;
        if(flashRemoteId.isEmpty()) title = bundle.getString("flash.remote.title.new");
        else title = bundle.getString("flash.remote.title.edit");
        tryAndCreateDialog(stage, "/ui/addNewFlashRemote.fxml", title, modal);
    }

    public Optional<FlashRemoteId> getResultOrEmpty() {
        return controller.getResult();
    }

    @Override
    protected void initialiseController(AddFlashRemoteController controller) {
        controller.initialise(currentSel);
    }
}
