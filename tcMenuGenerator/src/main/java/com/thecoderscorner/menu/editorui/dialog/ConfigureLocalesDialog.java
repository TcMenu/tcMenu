/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.ConfigureLocalesController;
import javafx.stage.Stage;

import java.util.List;
import java.util.Locale;
import java.util.Optional;


/** Shows the locale configuration dialog and has the ability to get the result */
public class ConfigureLocalesDialog extends BaseDialogSupport<ConfigureLocalesController> {
    private final List<Locale> localesActive;

    public ConfigureLocalesDialog(Stage stage, boolean modal, List<Locale> enabledLocales) {
        this.localesActive = enabledLocales;
        tryAndCreateDialog(stage, "/ui/configureLocales.fxml", bundle.getString("locale.dialog.title"), modal);
    }

    @Override
    protected void initialiseController(ConfigureLocalesController controller) throws Exception {
        controller.initialise(localesActive);
    }

    public Optional<List<Locale>> getResult() {
        return controller.getResult();
    }
}