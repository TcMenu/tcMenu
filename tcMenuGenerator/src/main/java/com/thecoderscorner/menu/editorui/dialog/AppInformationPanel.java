/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;

import java.io.IOException;

import static java.lang.System.Logger.Level.ERROR;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String GITHUB_PROJECT_URL = "https://github.com/davetcc/tcMenu/";

    private static final System.Logger logger = System.getLogger(AppInformationPanel.class.getSimpleName());
    private final MenuEditorController controller;
    private final ArduinoLibraryInstaller installer;
    private final CodePluginManager pluginManager;
    private CurrentProjectEditorUI editorUI;

    public AppInformationPanel(ArduinoLibraryInstaller installer, MenuEditorController controller,
                               CodePluginManager pluginManager, CurrentProjectEditorUI editorUI) {
        this.installer = installer;
        this.controller = controller;
        this.pluginManager = pluginManager;
        this.editorUI = editorUI;
    }

    public Node showEmptyInfoPanel() {
        VBox vbox = new VBox();
        vbox.setSpacing(5);

        Label docsLbl = new Label("TcMenu designer");
        docsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;-fx-padding: 4px;");
        vbox.getChildren().add(docsLbl);
        // add the documentation links
        labelWithUrl(vbox, LIBRARY_DOCS_URL, "Browse docs and watch starter videos (F1 at any time)", "libdocsurl");
        labelWithUrl(vbox, GITHUB_PROJECT_URL, "Please give us a star on github if you like this tool", "githuburl");

        // add the library installation status

        if(installer.statusOfAllLibraries().isUpToDate()) {
            Label lblTcMenuOK = new Label("Embedded Arduino libraries all up-to-date");
            lblTcMenuOK.setId("tcMenuStatusArea");
            lblTcMenuOK.getStyleClass().add("libsOK");
            vbox.getChildren().add(lblTcMenuOK);
        }
        else {
            Label libsNotOK = new Label("Embedded Arduino libraries need updating");
            libsNotOK.getStyleClass().add("libsNotOK");
            libsNotOK.setId("tcMenuStatusArea");
            vbox.getChildren().add(libsNotOK);
            Button installUpdates = new Button("Install library updates");
            installUpdates.setOnAction(controller::installLibraries);
            installUpdates.setId("installLibUpdates");
            vbox.getChildren().add(installUpdates);
        }

        try {
            makeDiffVersionLabel(vbox, "tcMenu");
            makeDiffVersionLabel(vbox, "IoAbstraction");
            makeDiffVersionLabel(vbox, "LiquidCrystalIO");
        }
        catch(Exception e) {
            logger.log(ERROR, "Library checks failed", e);
        }

        Label pluginLbl = new Label("Installed code generation plugins");
        pluginLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
        vbox.getChildren().add(pluginLbl);

        pluginManager.getLoadedPlugins().forEach(plugin -> {
            Label pluginInfoLbl = new Label("- " + plugin.getName() + " (" + plugin.getVersion() + ")");
            pluginInfoLbl.getStyleClass().add("pluginInfoLbl");
            vbox.getChildren().add(pluginInfoLbl);
        });

        return vbox;
    }

    private void labelWithUrl(VBox vbox, String urlToVisit, String title, String fxId) {
        Hyperlink docs = new Hyperlink(title);
        docs.setTooltip(new Tooltip(urlToVisit));
        docs.setStyle("-fx-vgap: 5px; -fx-border-insets: 0;");
        docs.setOnAction((event)->  editorUI.browseToURL(urlToVisit));
        docs.setId(fxId);
        vbox.getChildren().add(docs);
    }

    private void makeDiffVersionLabel(VBox vbox, String lib) throws IOException {
        var s = " - Arduino Library " + lib
                + " available: " + installer.getVersionOfLibrary(lib, true)
                + " installed: " + installer.getVersionOfLibrary(lib, false);

        var lbl = new Label(s);
        lbl.setId(lib + "Lib");
        vbox.getChildren().add(lbl);
    }

}
