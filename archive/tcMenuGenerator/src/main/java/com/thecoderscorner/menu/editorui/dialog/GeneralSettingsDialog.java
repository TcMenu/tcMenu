/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.menu.editorui.controller.GeneralSettingsController;
import com.thecoderscorner.menu.editorui.generator.AppVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.stage.Stage;


/** Example of displaying a splash page for a standalone JavaFX application */
public class GeneralSettingsDialog extends BaseDialogSupport<GeneralSettingsController> {
    private final ConfigurationStorage storage;
    private final AppVersionDetector detector;
    private final ArduinoLibraryInstaller installer;
    private final CodePluginManager manager;
    private final GlobalSettings settings;
    private final TccDatabaseUtilities databaseUtilities;
    private final String home;

    public GeneralSettingsDialog(Stage stage, ConfigurationStorage storage, AppVersionDetector detector,
                                 ArduinoLibraryInstaller installer, CodePluginManager manager,
                                 GlobalSettings settings, TccDatabaseUtilities databaseUtilities, String home) {
        this.storage = storage;
        this.detector = detector;
        this.installer = installer;
        this.manager = manager;
        this.settings = settings;
        this.databaseUtilities = databaseUtilities;
        this.home = home;

        tryAndCreateDialog(stage, "/ui/settingsDialog.fxml", bundle.getString("settings.dialog.title"), true);
    }


    @Override
    protected void initialiseController(GeneralSettingsController controller) {
        controller.initialise(storage, detector, installer, manager, settings, databaseUtilities, home);
    }
}