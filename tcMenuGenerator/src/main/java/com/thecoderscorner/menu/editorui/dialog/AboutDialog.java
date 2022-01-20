/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.AboutController;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;


/** Example of displaying a splash page for a standalone JavaFX application */
public class AboutDialog extends BaseDialogSupport<AboutController> {

    private final ConfigurationStorage storage;
    private final ArduinoLibraryInstaller installer;

    public AboutDialog(ConfigurationStorage storage, Stage stage, ArduinoLibraryInstaller installer, boolean modal) {
        this.storage = storage;
        this.installer = installer;
        tryAndCreateDialog(stage, "/ui/aboutDialog.fxml", "About tcMenu Designer", modal);
    }

    @Override
    protected void initialiseController(AboutController controller) throws Exception {
        controller.initialise(storage, installer);
    }
}