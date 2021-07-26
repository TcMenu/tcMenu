/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.manageditem.*;
import com.thecoderscorner.embedcontrol.jfx.panel.PanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.panel.RemoteConnectionPanel;
import com.thecoderscorner.menu.domain.*;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.domain.util.AbstractMenuItemVisitor;
import com.thecoderscorner.menu.domain.util.MenuItemHelper;
import com.thecoderscorner.menu.remote.*;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.commands.DialogMode;
import com.thecoderscorner.menu.remote.commands.MenuButtonType;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
    //
    // Begin JavaFX field bindings.
    //
    public Label statusLabel;
    public BorderPane mainBorderPane;
    public Label versionField;
    public ListView<PanelPresentable> connectionList;
    public ScrollPane containerArea;
    private GlobalSettings settings;
    private PanelPresentable currentlyDisplayed;

    public void initialise(GlobalSettings settings, List<PanelPresentable> initialPanels) {
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
        connectionList.getItems().addAll(initialPanels);
        connectionList.getSelectionModel().selectedItemProperty().addListener((observableValue, oldVal, newVal) -> {
            if(newVal != null) {
                try {
                    if(currentlyDisplayed == null || currentlyDisplayed.closePanelIfPossible()) {
                        newVal.presentPanelIntoArea(containerArea);
                        currentlyDisplayed = newVal;
                    }
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Form did not load", ButtonType.CLOSE);
                    alert.showAndWait();
                }
            }
        });
        connectionList.getSelectionModel().select(0);
    }

    public void createdConnection(RemoteConnectionPanel panel) {
        connectionList.getItems().add(panel);
    }

    private static class PanelPresentableListCell extends ListCell<PanelPresentable> {
        @Override
        public void updateItem(PanelPresentable item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) return;
            setText(item.getPanelName());
        }
    }
}

