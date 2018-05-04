/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.generator.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String YOUTUBE_VIDEO_URL = "https://youtu.be/NYYXh9iwI5s";

    private static final Logger logger = LoggerFactory.getLogger(AppInformationPanel.class);


    public  static Node showEmptyInfoPanel() {
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.getChildren().add(new Label("This item is not editable, select another item"));
        vbox.getChildren().add(new Rectangle(0,0,0,10));

        // add the documentation links
        vbox.getChildren().add(new Label("Documentation can be opened by pressing F1 or from the help menu."));
        Hyperlink docs = new Hyperlink(LIBRARY_DOCS_URL);
        docs.setOnAction((event)-> {
            try {
                Desktop.getDesktop().browse(new URI(LIBRARY_DOCS_URL));
            } catch (IOException | URISyntaxException e) {
                // not much we can do here really!
                logger.error("Could not open browser", e);
            }
        });
        vbox.getChildren().add(docs);
        Hyperlink youTube = new Hyperlink(YOUTUBE_VIDEO_URL);
        youTube.setOnAction((event)-> {
            try {
                Desktop.getDesktop().browse(new URI(YOUTUBE_VIDEO_URL));
            } catch (IOException | URISyntaxException e) {
                // not much we can do here really!
                logger.error("Could not open browser", e);
            }
        });
        vbox.getChildren().add(youTube);

        // add the library installation status

        if(ArduinoLibraryInstaller.findTcMenuInstall().isPresent()) {
            vbox.getChildren().add(new Label("TcMenu Arduino Library is installed"));
        }
        else {
            Label installLabel = new Label("TcMenu Arduino Library not yet installed");
            vbox.getChildren().add(installLabel);
            Hyperlink installLib = new Hyperlink("Install the TcMenu library");
            installLib.setOnAction(event -> {
                ArduinoLibraryInstaller installer = new ArduinoLibraryInstaller();
                installer.tryToInstallLibrary();
                installLib.setVisible(false);
                installLabel.setText("Installed TcMenu Library");
            });
            vbox.getChildren().add(installLib);
        }

        // and lastly the version

        vbox.getChildren().add(new Label(BuildVersionUtil.printableRegistrationInformation()));

        vbox.getChildren().add(new Label("tcMenu designer (C) 2018 by thecoderscorner.com."));
        return vbox;
    }

}
