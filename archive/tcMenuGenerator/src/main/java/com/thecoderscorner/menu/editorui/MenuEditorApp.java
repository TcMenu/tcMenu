/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui;

import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.embed.EditConnectionDialog;
import com.thecoderscorner.menu.editorui.embed.RemoteConnectionPanel;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.storage.JdbcTcMenuConfigurationStore;
import com.thecoderscorner.menu.editorui.storage.MenuEditorConfig;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.AlertUtil;
import com.thecoderscorner.menu.editorui.util.IHttpClient;
import com.thecoderscorner.menu.persist.VersionInfo;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.desktop.QuitStrategy;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.System.Logger.Level.ERROR;

/**
 * The application starting point for the JavaFX version of the application
 */
public class MenuEditorApp extends Application implements MenuEditorContext {
    private static MenuEditorContext INSTANCE = null;
    private static ResourceBundle designerBundle;
    public static final Locale EMPTY_LOCALE = Locale.of("");

    public MenuEditorConfig getAppContext() {
        return appContext;
    }

    private MenuEditorConfig appContext;
    private List<MenuEditorController> activeMainWindows = new ArrayList<>();
    private List<RemoteConnectionPanel> remoteConnectionWindows = new ArrayList<>();

    private static JdbcTcMenuConfigurationStore CONFIG_STORE;

    @Override
    public void start(Stage primaryStage) throws Exception {
        INSTANCE = this;
        startUpLogging();

        try {
            configureBundle(Locale.getDefault());
            appContext = new MenuEditorConfig();
            INSTANCE = this;
        } catch(Exception ex) {
            String msg;
            if(ex.getMessage().contains("Database lock acquisition failure")) {
                msg = "Please check if designer is already running, or you have opened the database in the .tcmenu directory.";
            } else {
                msg = "App did not start due to " + ex.getMessage() + ". See log for more details.";
            }
            System.getLogger(MenuEditorApp.class.getSimpleName()).log(ERROR, "Failed loading config", ex);
            AlertUtil.showAlertAndWait(AlertType.ERROR,"Could not load designer",
                    msg,
                    ButtonType.CLOSE);
            primaryStage.close(); // make sure the app closes here.
            return;
        }

        // !!must be on the main thread!!
        final String os = System.getProperty("os.name");
        if (os != null && os.startsWith("Mac")) {
            Desktop desktop = Desktop.getDesktop();
            desktop.setAboutHandler(e -> Platform.runLater(() -> lastActiveController().aboutMenuPressed(new ActionEvent())));
            desktop.setQuitStrategy(QuitStrategy.NORMAL_EXIT);
        }
        // !! end must be on the main thread!!

        CONFIG_STORE = appContext.getConfigStore();

        // if the chosen locale is not the default then force the locale.
        if(!CONFIG_STORE.getChosenLocale().equals(Locale.getDefault())) {
            configureBundle(CONFIG_STORE.getChosenLocale());
        }

        var pluginLoader = appContext.getPluginLoader();
        pluginLoader.loadPlugins();

        createPrimaryWindow(primaryStage);
    }

    public static void setContext(MenuEditorContext mockedContext) {
        INSTANCE = mockedContext;
    }


    public MenuEditorController createPrimaryWindow(Stage primaryStage) throws IOException {
        boolean initialWindow = true;
        if(primaryStage == null) {
            primaryStage = new Stage();
            initialWindow = false;
        }
        // load the main form
        primaryStage.setTitle(designerBundle.getString("main.editor.title"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/menuEditor.fxml"));
        loader.setResources(designerBundle);
        Pane myPane = loader.load();
        myPane.setStyle("-fx-font-size: " + GlobalSettings.defaultFontSize());
        MenuEditorController controller = loader.getController();

        var editorProject = appContext.newProject();
        var editorUI = editorProject.getEditorUI();
        editorUI.setStage(primaryStage, designerBundle);
        editorUI.setEditorProject(editorProject);

        controller.initialise(editorProject, appContext.getInstaller(),
                editorUI, appContext.getPluginLoader(), CONFIG_STORE,
                appContext.getLibraryVersionDetector(), initialWindow);

        var screenSize = Screen.getPrimary().getBounds();
        myPane.setPrefSize(Math.max(800, screenSize.getWidth() * .7), Math.max(600, screenSize.getHeight() * .7));

        Scene myScene = new Scene(myPane);
        BaseDialogSupport.getJMetro().setScene(myScene);
        primaryStage.setScene(myScene);
        primaryStage.show();
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon-sm.png"))));
        primaryStage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/menu-icon.png"))));

        activeMainWindows.add(controller);

        if(initialWindow) {
            showSplashIfNeeded(editorUI);
        }

        primaryStage.setOnCloseRequest((evt)-> {
            try {
                if(editorProject.isDirty()) {
                    evt.consume();
                    var btn = AlertUtil.showAlertAndWait(AlertType.CONFIRMATION, "There are unsaved changes, save first?", ButtonType.YES, ButtonType.NO);
                    if(btn.orElse(ButtonType.NO) == ButtonType.YES) {
                        editorProject.saveProject(CurrentEditorProject.EditorSaveMode.SAVE);
                    }
                }
                editorProject.close();
                activeMainWindows.remove(controller);
            }
            catch(Exception ex) {
                // ignored, we are trying to shutdown so just proceeed anyway.
            }

            if(activeMainWindows.isEmpty()) {
                Platform.exit();
                System.exit(0);
            }
        });
        return controller;
    }

    private void showSplashIfNeeded(CurrentProjectEditorUI editorUI) {
        var configStore = appContext.getConfigStore();
        var current = new VersionInfo(configStore.getVersion());
        if(!configStore.getLastRunVersion().equals(current) || System.getProperty("alwaysShowSplash", "N").equals("Y")) {
            Platform.runLater(()-> {
                configStore.setLastRunVersion(current);
                editorUI.showSplashScreen(themeName -> {
                    for(var controller : activeMainWindows) {
                        controller.themeDidChange(themeName);
                    }
                });
            });
        }

    }

    private MenuEditorController lastActiveController() {
        return activeMainWindows.stream().findFirst().orElseThrow();
    }

    public static ResourceBundle getBundle() {
        return designerBundle;
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

    public static MenuEditorContext getContext() {
        return INSTANCE;
    }

    public static ResourceBundle configureBundle(Locale locale) {
        designerBundle = ResourceBundle.getBundle("i18n.TcMenuUIText", locale);
        return designerBundle;
    }

    public void setCurrentTheme(String mode) {
        if(CONFIG_STORE != null) {
            CONFIG_STORE.setCurrentTheme(mode);
        }
    }

    public static String getCurrentTheme() {
        return CONFIG_STORE != null ? CONFIG_STORE.getCurrentTheme() : "darkMode";
    }

    public List<MenuEditorController> getAllMenuEditors() {
        return activeMainWindows;
    }

    public void embedControlRefresh() {
        for(var controller : getAllMenuEditors()) {
            controller.refreshEmbedControlMenu();
        }
    }

    public List<RemoteConnectionPanel> getAllActiveConnections() {
        return remoteConnectionWindows;
    }

    public List<TcMenuPersistedConnection> getAvailableConnections() {
        return appContext.getEcDataStore().getAllConnections();
    }

    public void createEmbedControlPanel(TcMenuPersistedConnection con) {
        var panel = new RemoteConnectionPanel(appContext.getRemoteContext(), MenuTree.ROOT,
                appContext.getExecutorService(), con);
        MenuEditorApp.getContext().embedControlRefresh();
        Stage stage = new Stage();
        stage.setWidth(800);
        stage.setHeight(600);
        stage.setTitle("EmbedControl " + con.getName() + " [" + con.getUuid() + "]");
        Scene myScene = new Scene((Parent) panel.getPanelToPresent(stage.getWidth()));
        stage.setScene(myScene);
        stage.show();

        stage.setOnCloseRequest(event -> {
            panel.closePanel();
            remoteConnectionWindows.remove(panel);
            embedControlRefresh();
        });
        remoteConnectionWindows.add(panel);
    }

    public void handleCreatingConnection(Stage stage) {
        var dlg = new EditConnectionDialog(stage, MenuEditorApp.getContext().getAppContext().getRemoteContext(), true);
        dlg.checkResult().ifPresent(tcMenuPersistedConnection -> {
            try {
                appContext.getEcDataStore().updateConnection(tcMenuPersistedConnection);
                embedControlRefresh();
            } catch (DataException e) {
                System.getLogger("main").log(ERROR, "Exception creating connection", e);
            }
        });

    }

    public void previewOnProject(Path path) {
        for(var editor : getAllMenuEditors()) {
            if(editor.getProject().getFileName().startsWith(path.toString())) {
                editor.OnShowPreviewWindow(null);
            }
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
        public String getRequestForString(String url) throws IOException {
            throw new IOException("Boom");
        }

        @Override
        public String postRequestForString(String url, String parameter, HttpDataType reqDataType) throws IOException {
            throw new IOException("Boom");
        }
    }
}
