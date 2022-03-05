/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class AboutController {
    public Label apiVersion;
    public Label buildDateLabel;

    public void initialise(ConfigurationStorage storage) throws IOException {
        apiVersion.setText(storage.getVersion());
        buildDateLabel.setText(storage.getBuildTimestamp());
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) apiVersion.getScene().getWindow();
        s.close();
    }
}
