/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.examples.simpleui;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * This UI shows how to get started building a desktop or mobile user interface
 * using Java for tcMenu. It's purposely kept as simple as possible while still
 * doing as many of the things most people need as possible.
 */
public class SimpleFxApp extends Application {
    public static final String MY_REMOTE_NAME = "DavesMac";
    public static final String MY_PORT_NAME = "/dev/cu.usbmodemFA1211";

    @Override
    public void start(Stage primaryStage) throws Exception {

        //
        // We build an RS232 connection to the Arduino device using the builder, it
        // does all the hard work and returns us a remote-controller
        //
        MenuTree menuTree = new MenuTree();
        RemoteMenuController remoteControl = new Rs232ControllerBuilder()
                .withLocalName(MY_REMOTE_NAME)
                .withMenuTree(menuTree)
                .withRs232(MY_PORT_NAME, 115200)
                .build();

        // At this point we build a JavaFX stage and load up our main window
        primaryStage.setTitle("JavaAPI -> tcMenu Arduino");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        // then we pass the menuTree and remoteControl to the windows controller.
        MainWindowController controller = loader.getController();
        controller.initialise(menuTree, remoteControl);

        // display the main window.
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();
    }
}
