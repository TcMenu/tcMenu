/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_LIB;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_LIB;

public class AboutController {
    public Label apiVersion;
    public Label buildDateLabel;
    public Label registeredLabel;

    public void initialise(ConfigurationStorage storage, ArduinoLibraryInstaller installer) throws IOException {
        apiVersion.setText(storage.getVersion());
        buildDateLabel.setText(storage.getBuildTimestamp());
        if(!storage.getRegisteredKey().isEmpty()) {
            registeredLabel.setText(storage.getRegisteredKey());
        }
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) apiVersion.getScene().getWindow();
        s.close();

    }
}
