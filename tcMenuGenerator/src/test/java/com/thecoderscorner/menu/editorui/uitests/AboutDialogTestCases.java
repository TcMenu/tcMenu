/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.dialog.AboutDialog;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_LIB;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_LIB;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class AboutDialogTestCases {

    private Stage stage;

    @Start
    public void onStart(Stage stage) throws IOException {
        this.stage = stage;
        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.getRegisteredKey()).thenReturn("UnitTesterII");
        when(storage.getVersion()).thenReturn("V1.0.2");
        when(storage.getBuildTimestamp()).thenReturn("20/10/2018 09:30");
        ArduinoLibraryInstaller installer = mock(ArduinoLibraryInstaller.class);
        AboutDialog dialog = new AboutDialog(storage, stage, installer, false);
    }

    @AfterEach
    public void tearDown() {
        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testAboutDialog(FxRobot robot) {
        verifyThat("#apiVersion", hasText("V1.0.2"));
        verifyThat("#buildDateLabel", hasText("20/10/2018 09:30"));
        verifyThat("#registeredLabel", hasText("UnitTesterII"));
    }
}
