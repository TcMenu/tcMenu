/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui;

import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.DefaultXmlPluginLoader;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.storage.JdbcTcMenuConfigurationStore;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUIImpl;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
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
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

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

import static java.lang.System.Logger.Level.ERROR;

/**
 * The application starting point for the JavaFX version of the application
 */
public class MenuEditorApp extends Application {
    private static MenuEditorApp INSTANCE = null;
    private volatile MenuEditorController controller;
    private static ResourceBundle designerBundle;
    public static final Locale EMPTY_LOCALE = Locale.of("");

    public ApplicationContext getAppContext() {
        return appContext;
    }

    private ApplicationContext appContext;
    private JdbcTcMenuConfigurationStore configStore;

    @Override
    public void start(Stage primaryStage) throws Exception {
        INSTANCE = this;
        startUpLogging();

        try {
            configureBundle(Locale.getDefault());
            appContext = new AnnotationConfigApplicationContext(MenuEditorConfig.class);
        } catch(Exception ex) {
            System.getLogger(MenuEditorApp.class.getSimpleName()).log(ERROR, "Failed loading config", ex);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Could not load designer");
            alert.setContentText("App did not start due to " + ex.getMessage() + ". See log for more details.");
            alert.setTitle("TcMenu Designer did not start");
            alert.showAndWait();
            primaryStage.close(); // make sure the app closes here.
            return;
        }

        Platform.runLater(() -> {
            final String os = System.getProperty("os.name");
            if (os != null && os.startsWith("Mac")) {
                Desktop desktop = Desktop.getDesktop();
                desktop.setAboutHandler(e -> Platform.runLater(() -> controller.aboutMenuPressed(new ActionEvent())));
                desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
            }
        });

        configStore = appContext.getBean(JdbcTcMenuConfigurationStore.class);

        // if the chosen locale is not the default then force the locale.
        if(!configStore.getChosenLocale().equals(Locale.getDefault())) {
            configureBundle(configStore.getChosenLocale());
        }

        // load the main form
        primaryStage.setTitle(designerBundle.getString("main.editor.title"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/menuEditor.fxml"));
        loader.setResources(designerBundle);
        Pane myPane = loader.load();
        controller = loader.getController();

        var libraryVersionDetector = appContext.getBean(LibraryVersionDetector.class);
        var pluginLoader = appContext.getBean(DefaultXmlPluginLoader.class);
        pluginLoader.ensurePluginsAreValid();
        pluginLoader.loadPlugins();

        var editorUI = appContext.getBean(CurrentProjectEditorUIImpl.class);
        editorUI.setStage(primaryStage, designerBundle);
        CurrentEditorProject project = appContext.getBean(CurrentEditorProject.class);
        editorUI.setEditorProject(project);

        controller.initialise(project, appContext.getBean(ArduinoLibraryInstaller.class),
                editorUI, appContext.getBean(DefaultXmlPluginLoader.class), configStore, libraryVersionDetector);

        Scene myScene = new Scene(myPane);
        BaseDialogSupport.getJMetro().setScene(myScene);
        primaryStage.setScene(myScene);
        primaryStage.show();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon-sm.png"))));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon.png"))));

        primaryStage.setOnCloseRequest((evt)-> {
            try {
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

    public static ResourceBundle getBundle() {
        return designerBundle;
    }

    public void createOrUpdateDirectoriesAsNeeded() {
        appContext.getBean(DefaultXmlPluginLoader.class).ensurePluginsAreValid();
    }

    private void startUpLogging() {
        var tcMenuHome = Paths.get(System.getProperty("user.home"), ".tcmenu");
        var logName = System.getProperty("devlog") != null ? "dev-logging" : "logging";
        var inputStream = MenuEditorApp.class.getResourceAsStream("/logconf/" + logName + ".properties");
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

    public static MenuEditorApp getInstance() {
        return INSTANCE;
    }

    public static ResourceBundle configureBundle(Locale locale) {
        designerBundle = ResourceBundle.getBundle("i18n.TcMenuUIText", locale);
        return designerBundle;
    }

    public void setCurrentTheme(String mode) {
        configStore.setCurrentTheme(mode);
    }

    public String getCurrentTheme() {
        return configStore.getCurrentTheme();
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
