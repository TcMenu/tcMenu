/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AboutController;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;


/** Example of displaying a splash page for a standalone JavaFX application */
public class AboutDialog extends BaseDialogSupport<AboutController> {

    private final ConfigurationStorage storage;

    public AboutDialog(ConfigurationStorage storage, Stage stage, boolean modal) {
        this.storage = storage;
        tryAndCreateDialog(stage, "/ui/aboutDialog.fxml", bundle.getString("about.dialog.title"), modal);
    }

    @Override
    protected void initialiseController(AboutController controller) throws Exception {
        controller.initialise(storage);
    }
}