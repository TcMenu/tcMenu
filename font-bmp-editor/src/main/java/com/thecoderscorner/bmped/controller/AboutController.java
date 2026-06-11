package com.thecoderscorner.bmped.controller;

import javafx.event.ActionEvent;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Properties;

public class AboutController {
    public Label apiVersion;
    public Label buildDateLabel;

    public void initialise() throws IOException {
        var versionProps = new Properties();
        versionProps.load(getClass().getResourceAsStream("/version.properties"));
        apiVersion.setText(versionProps.getProperty("build.version"));
        buildDateLabel.setText(versionProps.getProperty("build.timestamp"));
    }

    public void onClose(ActionEvent actionEvent) {
    }

}
