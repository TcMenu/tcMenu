/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.util.BuildVersionUtil;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    private final MenuEditorController controller;
    private ArduinoLibraryInstaller installer;

    public AppInformationPanel(ArduinoLibraryInstaller installer, MenuEditorController controller) {
        this.installer = installer;
        this.controller = controller;
    }

    public Node showEmptyInfoPanel() {
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

        if(installer.statusOfAllLibraries().isUpToDate()) {
            Label lblTcMenuOK = new Label("Embedded Arduino libraries all up-to-date");
            lblTcMenuOK.getStyleClass().add("libsOK");
            vbox.getChildren().add(lblTcMenuOK);
        }
        else {
            Label libsNotOK = new Label("Embedded Arduino libraries need updating");
            libsNotOK.getStyleClass().add("libsNotOK");
            vbox.getChildren().add(libsNotOK);
            Button installUpdates = new Button("Install library updates");
            installUpdates.setOnAction(this::installLibraries);
            vbox.getChildren().add(installUpdates);
        }

        try {
            vbox.getChildren().add(new Label(diffLibVersions("tcMenu")));
            vbox.getChildren().add(new Label(diffLibVersions("IoAbstraction")));
            vbox.getChildren().add(new Label(diffLibVersions("LiquidCrystalIO")));
        }
        catch(Exception e) {
            logger.error("Library checks failed", e);
        }

        // and lastly the version

        vbox.getChildren().add(new Label(BuildVersionUtil.printableRegistrationInformation()));
        vbox.getChildren().add(new Label("tcMenu designer (C) 2018 by thecoderscorner.com."));
        return vbox;
    }

    private void installLibraries(Event actionEvent) {
        try {
            installer.copyLibraryFromPackage("IoAbstraction");
            installer.copyLibraryFromPackage("tcMenu");
            installer.copyLibraryFromPackage("LiquidCrystalIO");
            controller.onTreeChangeSelection(MenuTree.ROOT);
        } catch (IOException e) {
            logger.error("Did not complete copying embedded files", e);
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to copy embedded files");
            alert.setTitle("Error while copying");
            alert.showAndWait();
        }
    }

    private String diffLibVersions(String lib) throws IOException {
        return " - Arduino Library " + lib
                + " available: " + installer.getVersionOfLibrary(lib, true)
                + " installed: " + installer.getVersionOfLibrary(lib, false);
    }

}
