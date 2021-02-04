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
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_LIB;

public class AboutController {
    public Label apiVersion;
    public Label tcMenuVersion;
    public Label ioAbstractionVersion;
    public Label liquidCrystalVersion;
    public Label taskManagerIO;
    public Label buildDateLabel;
    public Label registeredLabel;

    public void initialise(ConfigurationStorage storage, ArduinoLibraryInstaller installer) throws IOException {
        tcMenuVersion.setText(makeDiffVersionLabel(installer,"tcMenu"));
        ioAbstractionVersion.setText(makeDiffVersionLabel(installer,"IoAbstraction"));
        liquidCrystalVersion.setText(makeDiffVersionLabel(installer,"LiquidCrystalIO"));
        taskManagerIO.setText(makeDiffVersionLabel(installer,"TaskManagerIO"));
        apiVersion.setText(storage.getVersion());
        buildDateLabel.setText(storage.getBuildTimestamp());
        if(!storage.getRegisteredKey().isEmpty()) registeredLabel.setText(storage.getRegisteredKey());
    }

    private String makeDiffVersionLabel(ArduinoLibraryInstaller installer, String lib) throws IOException {
        return "available: " + installer.getVersionOfLibrary(lib, AVAILABLE_LIB)
             + " installed: " + installer.getVersionOfLibrary(lib, CURRENT_LIB);
    }


    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) apiVersion.getScene().getWindow();
        s.close();

    }
}
