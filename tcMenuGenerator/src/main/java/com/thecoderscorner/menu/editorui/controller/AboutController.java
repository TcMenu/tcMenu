/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutController {
    public Label apiVersion;
    public Label tcMenuVersion;
    public Label ioAbstractionVersion;
    public Label liquidCrystalVersion;

    public void initialise(ArduinoLibraryInstaller installer) throws IOException {
        tcMenuVersion.setText(installer.getVersionOfLibrary("tcMenu", true).toString());
        ioAbstractionVersion.setText(installer.getVersionOfLibrary("IoAbstraction", true).toString());
        liquidCrystalVersion.setText(installer.getVersionOfLibrary("LiquidCrystalIO", true).toString());
        apiVersion.setText(BuildVersionUtil.getVersionInfo());
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) apiVersion.getScene().getWindow();
        s.close();

    }
}
