/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui;

import com.thecoderscorner.menu.editorui.controller.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.controller.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.desktop.QuitStrategy;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import java.util.prefs.Preferences;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.ReleaseType;

/**
 * The application starting point for the JavaFX version of the application
 */
public class MenuEditorApp extends Application {

    private volatile MenuEditorController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        if(System.getProperty("java.util.logging.config.file") == null) {
            // if the logging was not set up, force it use logging.properties and ensure it's loaded.
            System.setProperty("java.util.logging.config.file", "logging.properties");
            try(var loggingProd = getClass().getResourceAsStream("/baseLoggingConfig.properties")) {
                LogManager manager = LogManager.getLogManager();
                manager.readConfiguration(loggingProd);
            }
            catch(Exception ex) {
                Alert alert = new Alert(AlertType.ERROR, "Logging configuration could not be loaded: " + ex.getMessage(), ButtonType.CLOSE);
                alert.showAndWait();
            }
        }

        final String os = System.getProperty ("os.name");
        if (os != null && os.startsWith ("Mac")) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(e -> Platform.runLater(() -> controller.aboutMenuPressed(new ActionEvent())));
            desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
        }

        createDirsIfNeeded();

        primaryStage.setTitle("Embedded Menu Designer");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/menuEditor.fxml"));
        Pane myPane = loader.load();

        controller = loader.getController();

        var stream = Preferences.userNodeForPackage(MenuEditorApp.class).get("ReleaseStream", ReleaseType.STABLE.toString());
        var httpClient = new SimpleHttpClient();
        LibraryVersionDetector libraryVersionDetector = new OnlineLibraryVersionDetector(httpClient, ReleaseType.valueOf(stream));

        PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();

        ConfigurationStorage prefsStore = new PrefsConfigurationStorage();

        DefaultXmlPluginLoader manager = new DefaultXmlPluginLoader(platforms, prefsStore);

        var homeDirectory = System.getProperty("homeDirectoryOverride", System.getProperty("user.home"));
        ArduinoLibraryInstaller installer = new ArduinoLibraryInstaller(homeDirectory, libraryVersionDetector, manager,
                            prefsStore, true);

        platforms.setInstaller(installer);

        manager.loadPlugins(configuredPluginPaths());

        CurrentProjectEditorUIImpl editorUI = new CurrentProjectEditorUIImpl(manager, primaryStage, platforms,
                installer, prefsStore);

        FileBasedProjectPersistor persistor = new FileBasedProjectPersistor();

        CurrentEditorProject project = new CurrentEditorProject(editorUI, persistor);

        controller.initialise(project, installer, editorUI, manager, prefsStore, libraryVersionDetector);

        Scene myScene = new Scene(myPane);
        primaryStage.setScene(myScene);
        primaryStage.show();

        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/img/menu-icon.png")));

        primaryStage.setOnCloseRequest((evt)-> {
            try {
                var streamStr = libraryVersionDetector.getReleaseType().toString();
                Preferences.userNodeForPackage(MenuEditorApp.class).put("ReleaseStream", streamStr);
                controller.persistPreferences();
                if(project.isDirty()) {
                    evt.consume();
                    Alert alert = new Alert(AlertType.CONFIRMATION);
                    alert.setTitle("Are you sure");
                    alert.setHeaderText("There are unsaved changes, save first?");
                    if(alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                        project.saveProject(CurrentEditorProject.EditorSaveMode.SAVE);
                    }
                }
            }
            catch(Exception ex) {
                // ignored, we are trying to shutdown so just proceeed anyway.
            }
            Platform.exit();
            System.exit(0);
        });
    }

    public static List<Path> configuredPluginPaths() {
        var list = new ArrayList<Path>();
        var defPluginPath = Paths.get(System.getProperty("user.home"), ".tcmenu", "plugins");
        var additionalPlugins = System.getProperty("additionalPluginsDir");
        list.add(defPluginPath);

        if(additionalPlugins != null) {
            list.add(Paths.get(additionalPlugins));
        }
        return list;
    }

    private void createDirsIfNeeded() {
        var homeDir = Paths.get(System.getProperty("user.home"));
        try {
            Path menuDir = homeDir.resolve(".tcmenu/logs");
            if(!Files.exists(menuDir)) {
                Files.createDirectories(menuDir);
            }
            Path pluginDir = homeDir.resolve(".tcmenu/plugins");
            if(!Files.exists(pluginDir)) {
                try {
                    Files.createDirectories(pluginDir);
                    InputStream resourceAsStream = getClass().getResourceAsStream("/packaged-plugins/initialPlugins.zip");
                    OnlineLibraryVersionDetector.extractFilesFromZip(pluginDir, resourceAsStream);
                }
                catch(Exception ex) {
                    // ignored
                }
            }

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Error creating user directory", ButtonType.CLOSE);
            alert.setContentText("Couldn't create user directory: " + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * This mock client is used during test to fail every single web call, to ensure the UI works in
     * this scenario
     */
    private static class MockHttpClient implements IHttpClient {
        @Override
        public byte[] postRequestForBinaryData(String url, String parameter, HttpDataType reqDataType) throws IOException {
            throw new IOException("Boom");
        }

        @Override
        public String postRequestForString(String url, String parameter, HttpDataType reqDataType) throws IOException {
            throw new IOException("Boom");
        }
    }
}
