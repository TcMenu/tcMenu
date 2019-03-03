/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.dialog.AboutDialog;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.io.IOException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class AboutDialogTestCases {

    @Start
    public void onStart(Stage stage) throws IOException {
        ArduinoLibraryInstaller installer = mock(ArduinoLibraryInstaller.class);
        when(installer.getVersionOfLibrary("tcMenu", true)).thenReturn(new VersionInfo("1.0.1"));
        when(installer.getVersionOfLibrary("IoAbstraction", true)).thenReturn(new VersionInfo("1.0.2"));
        when(installer.getVersionOfLibrary("LiquidCrystalIO", true)).thenReturn(new VersionInfo("1.0.0"));

        AboutDialog dialog = new AboutDialog(stage, installer, false);
    }

    @Test
    public void testAboutDialog(FxRobot robot) {
        verifyThat("#tcMenuVersion", hasText("V1.0.1"));
        verifyThat("#ioAbstractionVersion", hasText("V1.0.2"));
        verifyThat("#liquidCrystalVersion", hasText("V1.0.0"));

        String ver = BuildVersionUtil.getVersionInfo();

        verifyThat("#apiVersion", hasText(ver));

        robot.clickOn(".button:default");
    }
}
