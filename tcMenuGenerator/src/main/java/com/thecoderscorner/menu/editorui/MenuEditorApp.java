/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui;

import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.FileBasedProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.storage.PrefsConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.editorui.util.SimpleHttpClient;
import com.thecoderscorner.menu.persist.ReleaseType;
import com.thecoderscorner.menu.persist.VersionInfo;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * The application starting point for the JavaFX version of the application
 */
public class MenuEditorApp extends Application {

    private volatile MenuEditorController controller;
    private static ResourceBundle designerBundle;
    public static final Locale EMPTY_LOCALE = new Locale("");

    @Override
    public void start(Stage primaryStage) throws Exception {
        startUpLogging();

        Platform.runLater(() -> {
            final String os = System.getProperty("os.name");
            if (os != null && os.startsWith("Mac")) {
                Desktop desktop = Desktop.getDesktop();
                desktop.setAboutHandler(e -> Platform.runLater(() -> controller.aboutMenuPressed(new ActionEvent())));
                desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
            }
        });

        ConfigurationStorage prefsStore = new PrefsConfigurationStorage();

        configureBundle(prefsStore.getChosenLocale());

        createOrUpdateDirectoriesAsNeeded(prefsStore);

        primaryStage.setTitle(designerBundle.getString("main.editor.title"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/menuEditor.fxml"));
        loader.setResources(designerBundle);
        Pane myPane = loader.load();

        controller = loader.getController();

        LibraryVersionDetector libraryVersionDetector = createLibraryVersionDetector();

        PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();

        DefaultXmlPluginLoader manager = new DefaultXmlPluginLoader(platforms, prefsStore, true);

        ArduinoLibraryInstaller installer = new ArduinoLibraryInstaller(libraryVersionDetector, manager, prefsStore);

        platforms.setInstallerConfiguration(installer, prefsStore);

        manager.loadPlugins();

        var homeDirectory = System.getProperty("homeDirectoryOverride", System.getProperty("user.home"));
        var editorUI = new CurrentProjectEditorUIImpl(manager, primaryStage, platforms, installer, prefsStore, libraryVersionDetector, homeDirectory, designerBundle);

        FileBasedProjectPersistor persistor = new FileBasedProjectPersistor();

        CurrentEditorProject project = new CurrentEditorProject(editorUI, persistor, prefsStore);
        editorUI.setEditorProject(project);

        controller.initialise(project, installer, editorUI, manager, prefsStore, libraryVersionDetector);

        Scene myScene = new Scene(myPane);
        BaseDialogSupport.getJMetro().setScene(myScene);

        primaryStage.setScene(myScene);
        primaryStage.show();

        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon-sm.png"))));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon.png"))));

        primaryStage.setOnCloseRequest((evt)-> {
            try {
                var streamStr = libraryVersionDetector.getReleaseType().toString();
                Preferences.userNodeForPackage(MenuEditorApp.class).put("ReleaseStream", streamStr);
                controller.persistPreferences();
                if(project.isDirty()) {
                    evt.consume();
                    Alert alert = new Alert(AlertType.CONFIRMATION, "There are unsaved changes, save first?",
                            ButtonType.YES, ButtonType.NO);
                    BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());

                    alert.setTitle("Are you sure");
                    alert.setHeaderText("");
                    if(alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
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

    public static LibraryVersionDetector createLibraryVersionDetector() {
        var stream = Preferences.userNodeForPackage(MenuEditorApp.class).get("ReleaseStream", ReleaseType.STABLE.toString());
        var httpClient = new SimpleHttpClient();

        var urlBase = "https://www.thecoderscorner.com";

        if(System.getProperty("localTccService") != null) {
            urlBase = System.getProperty("localTccService");
            System.getLogger("Main").log(WARNING, "Overriding the TCC service to " + urlBase);
        }

        LibraryVersionDetector libraryVersionDetector = new OnlineLibraryVersionDetector(urlBase, httpClient, ReleaseType.valueOf(stream));
        return libraryVersionDetector;
    }

    public static ResourceBundle getBundle() {
        return designerBundle;
    }

    public static void createOrUpdateDirectoriesAsNeeded(ConfigurationStorage storage) {
        var homeDir = Paths.get(System.getProperty("user.home"));
        try {
            Path menuDir = homeDir.resolve(".tcmenu/logs");
            if(!Files.exists(menuDir)) {
                Files.createDirectories(menuDir);
            }
            Path pluginDir = homeDir.resolve(".tcmenu/plugins");
            var current = new VersionInfo(storage.getVersion());
            var noPluginDir = !isDirectoryPresentAndPopulated(pluginDir);
            if(!storage.getLastRunVersion().equals(current) || noPluginDir) {
                try {
                    if(noPluginDir) Files.createDirectories(pluginDir);

                    if(Files.find(pluginDir, 2, (path, basicFileAttributes) -> path.endsWith(".git") || path.endsWith(".development")).findFirst().isPresent()) {
                        System.getLogger("Main").log(WARNING, "Not upgrading core plugins, this is a development system");
                        return;
                    }

                    try(var resourceAsStream = MenuEditorApp.class.getResourceAsStream("/plugins/InitialPlugins.zip")) {
                        OnlineLibraryVersionDetector.extractFilesFromZip(pluginDir, resourceAsStream);
                    }
                }
                catch(Exception ex) {
                    System.getLogger("Main").log(ERROR, "failed to prepare directory structure", ex);
                }
            }

        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR, "Error creating user directory", ButtonType.CLOSE);
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.setContentText("Couldn't create user directory: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public static boolean isDirectoryPresentAndPopulated(Path path) throws IOException {
        if(!Files.exists(path)) return false;
        if (Files.isDirectory(path)) {
            return Files.find(path, 1, (p, a) -> p.getFileName().toString().startsWith("core-"))
                    .findFirst().isPresent();
        }
        return false;
    }

    private void startUpLogging() {
        var logName = System.getProperty("devlog") != null ? "dev-logging" : "logging";
        var inputStream = MenuEditorApp.class.getResourceAsStream("/logconf/" + logName + ".properties");
        try
        {
            LogManager.getLogManager().readConfiguration(inputStream);
        }
        catch (final IOException e)
        {
            Logger.getAnonymousLogger().severe("Could not load default logger:" + e.getMessage());
        }
    }

    public static ResourceBundle configureBundle(Locale locale) {
        designerBundle = ResourceBundle.getBundle("i18n.TcMenuUIText", locale);
        return designerBundle;
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
