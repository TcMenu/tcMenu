/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfxapp;

import com.thecoderscorner.embedcontrol.jfxapp.dialog.MainWindowController;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EmbedControlApp extends Application {
    private MainWindowController controller;
    private final System.Logger logger = System.getLogger(EmbedControlApp.class.getSimpleName());
    private Stage primaryStage;
    private RemoteUiEmbedControlContext controlContext;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;

        startUpLogging();

        var applicationContext = new EmbedControlAppConfig();
        controlContext = applicationContext.getRemoteContext();

        // At this point we build a JavaFX stage and load up our main window
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/fximg/large_icon.png"))));
        primaryStage.setTitle("embedCONTROL desktop");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        // then we pass the menuTree and remoteControl to the Windows controller.
        controller = loader.getController();
        controlContext.initialize(controller);

        // display the main window.
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();

        primaryStage.setOnCloseRequest((evt)-> {
            Platform.exit();
            System.exit(0);
        });
    }

    private void startUpLogging() {
        var logName = System.getProperty("devlog") != null ? "dev-logging" : "logging";
        var inputStream = EmbedControlApp.class.getResourceAsStream("/logconf/" + logName + ".properties");
        try
        {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
        catch (final IOException e)
        {
            Logger.getAnonymousLogger().severe("Could not load default logger:" + e.getMessage());
        }
    }


}
