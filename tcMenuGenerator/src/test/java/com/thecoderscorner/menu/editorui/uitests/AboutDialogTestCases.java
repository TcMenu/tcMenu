/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.uitests;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.dialog.AboutDialog;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

@ExtendWith(ApplicationExtension.class)
public class AboutDialogTestCases {

    private Stage stage;

    @Start
    public void onStart(Stage stage) {
        this.stage = stage;
        ConfigurationStorage storage = mock(ConfigurationStorage.class);
        when(storage.getRegisteredKey()).thenReturn("UnitTesterII");
        when(storage.getVersion()).thenReturn("V1.0.2");
        when(storage.getBuildTimestamp()).thenReturn("20/10/2018 09:30");
        new AboutDialog(storage, stage, false);
    }

    @AfterEach
    public void tearDown() {
        Platform.runLater(()-> stage.close());
    }

    @Test
    public void testAboutDialog(FxRobot robot) {
        verifyThat("#apiVersion", hasText("V1.0.2"));
        verifyThat("#buildDateLabel", hasText("20/10/2018 09:30"));
        robot.clickOn(".button:default");
    }
}
