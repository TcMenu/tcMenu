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

public class SimpleFxApp extends Application {
    public static final String MY_REMOTE_NAME = "DavesMac";
    public static final String MY_PORT_NAME = "/dev/cu.usbmodemFA1211";

    @Override
    public void start(Stage primaryStage) throws Exception {

        MenuTree menuTree = new MenuTree();
        RemoteMenuController remoteControl = new Rs232ControllerBuilder()
                .withLocalName(MY_REMOTE_NAME)
                .withMenuTree(menuTree)
                .withRs232(MY_PORT_NAME, 115200)
                .build();

        primaryStage.setTitle("JavaAPI -> tcMenu Arduino");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        MainWindowController controller = loader.getController();
        controller.initialise(menuTree, remoteControl);

        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();
    }
}
