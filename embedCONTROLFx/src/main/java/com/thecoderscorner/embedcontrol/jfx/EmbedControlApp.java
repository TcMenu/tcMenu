/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.embedcontrol.jfx;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.jfx.panel.AboutPanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.panel.NewConnectionPanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.panel.RemoteConnectionPanel;
import com.thecoderscorner.embedcontrol.jfx.panel.SettingsPanelPresentable;
import com.thecoderscorner.embedcontrol.jfx.rs232.Rs232SerialFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class EmbedControlApp extends Application {

    private static final Object metroLock = new Object();
    private static JMetro jMetro = null;
    private static GlobalSettings settings;
    private final ScheduledExecutorService coreExecutor = Executors.newScheduledThreadPool(4);
    private MainWindowController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
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

        // At this point we build a JavaFX stage and load up our main window
        primaryStage.getIcons().add(new Image(
                Objects.requireNonNull(getClass().getResourceAsStream("/img/large_icon.png"))));
        primaryStage.setTitle("embedCONTROL desktop");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/mainWindow.fxml"));
        Pane myPane = loader.load();

        Rs232SerialFactory serialFactory = new Rs232SerialFactory(settings, coreExecutor);

        settings = new GlobalSettings();
        settings.load();

        // then we pass the menuTree and remoteControl to the windows controller.
        controller = loader.getController();
        controller.initialise(settings, List.of(
                new AboutPanelPresentable(),
                new SettingsPanelPresentable(settings),
                new NewConnectionPanelPresentable(serialFactory, this::creatorConsumer, settings, coreExecutor)
        ));

        // display the main window.
        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        getJMetro().setScene(myScene);
        primaryStage.show();
    }

    private void creatorConsumer(ConnectionCreator connectionCreator) {
        var panel = new RemoteConnectionPanel(connectionCreator, settings, coreExecutor);
        controller.createdConnection(panel);
    }

    /**
     * Get the JMetro object that we use as the theme.
     * @return the jmetro object.
     */
    public static JMetro getJMetro() {
        synchronized (metroLock) {
            if (jMetro == null) {
                Style style = settings.isDarkMode() ? Style.DARK : Style.LIGHT;
                jMetro = new JMetro(style);
            }
        }
        return jMetro;
    }


}
