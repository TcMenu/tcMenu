/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ConfigureLocalesController;
import com.thecoderscorner.menu.persist.PropertiesLocaleEnabledHandler;
import javafx.stage.Stage;

/** Shows the locale configuration dialog and has the ability to get the result */
public class ConfigureLocalesDialog extends BaseDialogSupport<ConfigureLocalesController> {
    private final PropertiesLocaleEnabledHandler localeHandler;

    public ConfigureLocalesDialog(Stage stage, boolean modal, PropertiesLocaleEnabledHandler localeHandler) {
        this.localeHandler = localeHandler;
        tryAndCreateDialog(stage, "/ui/configureLocales.fxml", bundle.getString("locale.dialog.title"), modal);
    }

    @Override
    protected void initialiseController(ConfigureLocalesController controller) throws Exception {
        controller.initialise(localeHandler);
    }
}