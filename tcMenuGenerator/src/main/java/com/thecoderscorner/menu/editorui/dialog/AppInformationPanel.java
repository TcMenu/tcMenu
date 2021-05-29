/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.AVAILABLE_PLUGIN;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.CURRENT_PLUGIN;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String GITHUB_PROJECT_URL = "https://github.com/davetcc/tcMenu/";
    public static final String GETTING_STARTED_PAGE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-overview-quick-start/";
    public static final String FONTS_GUIDE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/using-custom-fonts-in-menu/";
    public static final String TCC_FORUM_PAGE = "http://www.thecoderscorner.com/jforum/recentTopics/list.page";

    private final MenuEditorController controller;
    private final ArduinoLibraryInstaller installer;
    private final CodePluginManager pluginManager;
    private final CurrentProjectEditorUI editorUI;
    private final LibraryVersionDetector libraryVersionDetector;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ConfigurationStorage storage;
    private VBox libraryInfoVBox;

    public AppInformationPanel(ArduinoLibraryInstaller installer, MenuEditorController controller,
                               CodePluginManager pluginManager, CurrentProjectEditorUI editorUI,
                               LibraryVersionDetector libraryVersionDetector, ConfigurationStorage storage) {
        this.installer = installer;
        this.controller = controller;
        this.pluginManager = pluginManager;
        this.editorUI = editorUI;
        this.libraryVersionDetector = libraryVersionDetector;
        this.storage = storage;
    }

    public Node showEmptyInfoPanel() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);

        String title = "TcMenu designer";
        if(storage.getReleaseType() == ConfigurationStorage.TcMenuReleaseType.BETA) {
            title += " This is a BETA version - don't use in production";
        }
        Label docsLbl = new Label(title);
        docsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
        vbox.getChildren().add(docsLbl);

        Label appPath = new Label("File: " + controller.getProject().getFileName());
        vbox.getChildren().add(appPath);
        var options = controller.getProject().getGeneratorOptions();
        var appNameInfo = String.format("Name: %s, (%s)", options.getApplicationName(), options.getApplicationUUID().toString());
        Label appName = new Label(appNameInfo);
        vbox.getChildren().add(appName);

        // add the documentation links
        labelWithUrl(vbox, LIBRARY_DOCS_URL, "Browse docs and watch starter videos (F1 at any time)", "libdocsurl");
        labelWithUrl(vbox, GITHUB_PROJECT_URL, "Please give us a star on github if you like this tool", "githuburl");

        libraryInfoVBox = new VBox(3.0);
        checkAndReportItems(libraryInfoVBox);
        vbox.getChildren().add(libraryInfoVBox);
        return vbox;
    }

    private void checkAndReportItems(VBox vbox) {
        vbox.getChildren().clear();
        vbox.getChildren().add(new Label("Reading version information.."));
        var fr = executor.submit(() -> {
            if(libraryVersionDetector.availableVersionsAreValid(true)) {
                Platform.runLater(this::redrawTheTitlePage);
            }
        });
    }

    private void redrawTheTitlePage() {
        var vbox = libraryInfoVBox;
        vbox.getChildren().clear();
        boolean needRefresh = false;
        if(installer.getArduinoDirectory().isEmpty() && storage.isUsingArduinoIDE()) {
            Label setManually = new Label("Set Arduino directory from Edit -> General Settings");
            setManually.setId("tcMenuStatusArea");
            setManually.getStyleClass().add("libsNotOK");
            vbox.getChildren().add(setManually);
            needRefresh = true;
        }
        else if(storage.isUsingArduinoIDE() && installer.statusOfAllLibraries().isUpToDate()) {
            Label lblTcMenuOK = new Label("Embedded Arduino libraries all up-to-date");
            lblTcMenuOK.setId("tcMenuStatusArea");
            lblTcMenuOK.getStyleClass().add("libsOK");
            vbox.getChildren().add(lblTcMenuOK);
        }
        else if(storage.isUsingArduinoIDE()) {
            Label libsNotOK = new Label("Libraries are out of date, see Edit -> General Settings");
            libsNotOK.getStyleClass().add("libsNotOK");
            libsNotOK.setId("tcMenuStatusArea");
            vbox.getChildren().add(libsNotOK);
            needRefresh = true;
        }

        boolean pluginUpdateNeeded = (pluginManager.getLoadedPlugins().size() == 0);

        for(var plugin : pluginManager.getLoadedPlugins()) {
            var availableVersion = getVersionOfLibraryOrError(plugin, AVAILABLE_PLUGIN);
            var installedVersion = getVersionOfLibraryOrError(plugin, CURRENT_PLUGIN);
            pluginUpdateNeeded = pluginUpdateNeeded || !installedVersion.equals(availableVersion);
        }

        if(pluginUpdateNeeded) {
            var noPlugins = pluginManager.getLoadedPlugins().isEmpty();
            var pluginLabel = new Label(noPlugins ? "No plugins installed, fix in Edit -> General Settings" : "Plugin updates are available in Edit -> General Settings");
            pluginLabel.setId("tcMenuPluginIndicator");
            pluginLabel.getStyleClass().add("libsNotOK");
            vbox.getChildren().add(pluginLabel);
            needRefresh = true;
        }
        else {
            Label lblPluginsOk = new Label("All plugins are up to date.");
            lblPluginsOk.setId("tcMenuPluginIndicator");
            lblPluginsOk.getStyleClass().add("libsOK");
            vbox.getChildren().add(lblPluginsOk);
        }

        if(needRefresh) {
            var refreshButton = new Button("Refresh library status");
            refreshButton.setOnAction(actionEvent -> controller.presentInfoPanel());
            vbox.getChildren().add(refreshButton);
        }
    }

    private VersionInfo getVersionOfLibraryOrError(CodePluginConfig plugin, InstallationType type) {
        try {
            return installer.getVersionOfLibrary(plugin.getModuleName(), type);
        } catch (IOException e) {
            return VersionInfo.ERROR_VERSION;
        }
    }

    private void labelWithUrl(VBox vbox, String urlToVisit, String title, String fxId) {
        Hyperlink docs = new Hyperlink(title);
        docs.setTooltip(new Tooltip(urlToVisit));
        docs.setStyle("-fx-vgap: 5px; -fx-border-insets: 0;");
        docs.setOnAction((event)->  editorUI.browseToURL(urlToVisit));
        docs.setId(fxId);
        vbox.getChildren().add(docs);
    }
}
