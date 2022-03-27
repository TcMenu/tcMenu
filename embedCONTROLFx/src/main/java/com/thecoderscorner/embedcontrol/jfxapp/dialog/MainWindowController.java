/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.PanelPresentable;
import com.thecoderscorner.embedcontrol.jfxapp.panel.RemoteConnectionPanel;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.System.Logger.Level.*;
import static java.lang.System.Logger.Level.ERROR;

/**
 * This is the window controller, responsible for all actions to do the main window. It listens to change
 * from the tcMenu remote control and renders them to the display, and sends commands to the Arduino when
 * changes are made locally.
 * <p>
 * I've tried to keep this class as simple as possible, avoiding functional style and optimisation.
 * <p>
 * I've also avoided doing any more styling than absolutely required so the UI looks quite basic, get
 * out your crayons and make your own look pretty!
 * <p>
 * You'll notice that tcMenu JavaAPI does make use of both immutability and the 'visitor pattern', this
 * allows you to use most of the API objects on any thread.
 */
public class MainWindowController {
    private final System.Logger logger = System.getLogger(MainWindowController.class.getSimpleName());
    //
    // Begin JavaFX field bindings.
    //
    public Label statusLabel;
    public BorderPane mainBorderPane;
    public Label versionField;
    public ListView<PanelPresentable> connectionList;
    public BorderPane detailPane;
    private GlobalSettings settings;
    private PanelPresentable currentlyDisplayed;

    public void initialise(GlobalSettings settings, ObservableList<PanelPresentable> initialPanels) {
        this.settings = settings;

        try {
            InputStream resourceAsStream = getClass().getResourceAsStream("/version.properties");
            Properties props = new Properties();
            props.load( resourceAsStream );
            versionField.setText("Version " + props.getProperty("build.version"));
        } catch (IOException e) {
            versionField.setText("Version ???");
        }


        connectionList.setCellFactory(list -> new PanelPresentableListCell());
        connectionList.setItems(initialPanels);
        connectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if(newVal != null) {
                try {
                    logger.log(INFO, "Change panel to ", newVal.getPanelName());
                    if(currentlyDisplayed == null || currentlyDisplayed.closePanelIfPossible()) {
                        clearBorderPanel();
                        logger.log(INFO, "Present new panel ", newVal.getPanelName());
                        newVal.presentPanelIntoArea(detailPane);
                        currentlyDisplayed = newVal;
                    }
                } catch (Exception e) {
                    logger.log(ERROR, "Failed to open the new panel", e);
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Form did not load", ButtonType.CLOSE);
                    alert.showAndWait();
                }
            }
        });
        connectionList.getSelectionModel().select(0);
    }

    private void clearBorderPanel() {
        detailPane.setTop(null);
        detailPane.setCenter(null);
        detailPane.setBottom(null);
        detailPane.setRight(null);
    }

    public void createdConnection(RemoteConnectionPanel panel) {
        connectionList.getItems().add(panel);
        connectionList.getSelectionModel().select(panel);
    }

    public void selectPanel(PanelPresentable panelPresentable) {
        connectionList.getSelectionModel().select(panelPresentable);
    }

    private static class PanelPresentableListCell extends ListCell<PanelPresentable> {
        @Override
        public void updateItem(PanelPresentable item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) {
                setText("");
            }
            else {
                setText(item.getPanelName());
            }
        }
    }
}

