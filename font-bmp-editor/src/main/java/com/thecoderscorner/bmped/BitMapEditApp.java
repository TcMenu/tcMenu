package com.thecoderscorner.bmped;

import com.thecoderscorner.bmped.controller.MainWindowController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class BitMapEditApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        startUpLogging();
        var logger = Logger.getLogger(getClass().getSimpleName());
        logger.info("Starting Bitmap Editor application, loading app");

        primaryStage.setTitle("Embedded Font and Bitmap Editor");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxui/mainWindow.fxml"));
        Pane myPane = loader.load();
        myPane.setStyle("-fx-font-size: " + 16);
        MainWindowController controller = loader.getController();
        controller.initialize(primaryStage);

        var screenSize = Screen.getPrimary().getBounds();
        myPane.setPrefSize(Math.max(800, screenSize.getWidth() * .7), Math.max(600, screenSize.getHeight() * .7));

        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon-sm.png"))));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon.png"))));

    }

    private void startUpLogging() {
        var tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
        var logName = System.getProperty("devlog") != null ? "dev-logging" : "logging";
        var inputStream = getClass().getResourceAsStream("/logconf/" + logName + ".properties");
        try
        {
            Path menuDir = tcMenuHome.resolve(".tcmenu/logs");
            if(!Files.exists(menuDir)) {
                Files.createDirectories(menuDir);
            }

            LogManager.getLogManager().readConfiguration(inputStream);
        }
        catch (Exception e)
        {
            Logger.getAnonymousLogger().severe("Could not load default logger:" + e.getMessage());
        }
    }

}
