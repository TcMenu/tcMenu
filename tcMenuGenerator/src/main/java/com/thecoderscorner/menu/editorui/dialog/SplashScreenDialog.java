/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.SplashScreenController;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.stage.Stage;

import java.util.function.Consumer;


/** Example of displaying a splash page for a standalone JavaFX application */
public class SplashScreenDialog extends BaseDialogSupport<SplashScreenController> {
    private final CurrentProjectEditorUI editorUI;
    private final Consumer<String> themeListener;

    public SplashScreenDialog(Stage stage, CurrentProjectEditorUI editorUI, Consumer<String> themeListener, boolean modal) {
        this.editorUI = editorUI;
        this.themeListener = themeListener;
        tryAndCreateDialog(stage, "/ui/splashScreen.fxml", "TcMenu Designer", modal);
    }

    @Override
    protected void initialiseController(SplashScreenController controller) {
        controller.initialise(editorUI, themeListener);
    }
}