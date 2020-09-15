/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_LIB;

public class AboutController {
    public Label apiVersion;
    public Label tcMenuVersion;
    public Label ioAbstractionVersion;
    public Label liquidCrystalVersion;
    public Label taskManagerIO;
    public Label buildDateLabel;
    public Label registeredLabel;

    public void initialise(ConfigurationStorage storage, ArduinoLibraryInstaller installer) throws IOException {
        tcMenuVersion.setText(installer.getVersionOfLibrary("tcMenu", AVAILABLE_LIB).toString());
        ioAbstractionVersion.setText(installer.getVersionOfLibrary("IoAbstraction", AVAILABLE_LIB).toString());
        liquidCrystalVersion.setText(installer.getVersionOfLibrary("LiquidCrystalIO", AVAILABLE_LIB).toString());
        taskManagerIO.setText(installer.getVersionOfLibrary("TaskManagerIO", AVAILABLE_LIB).toString());
        apiVersion.setText(storage.getVersion());
        buildDateLabel.setText(storage.getBuildTimestamp());
        if(!storage.getRegisteredKey().isEmpty()) registeredLabel.setText(storage.getRegisteredKey());
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) apiVersion.getScene().getWindow();
        s.close();

    }
}
