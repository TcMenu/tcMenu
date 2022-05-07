/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.dialog;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.controller.MenuEditorController;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptionsBuilder;
import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.util.EnumWithStringValue;
import com.thecoderscorner.menu.persist.VersionInfo;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport.getJMetro;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;
import static javafx.collections.FXCollections.observableArrayList;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String GITHUB_PROJECT_URL = "https://github.com/davetcc/tcMenu/";
    public static final String GETTING_STARTED_PAGE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-overview-quick-start/";
    public static final String FONTS_GUIDE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/using-custom-fonts-in-menu/";
    public static final String TCC_FORUM_PAGE = "https://www.thecoderscorner.com/jforum/recentTopics/list.page";
    public static final String EEPROM_HELP_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-eeprom-integrations/";
    public static final String AUTHENTICATOR_HELP_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/secure-menuitem-pins-and-remotes/";
    public static final String SPONSOR_TCMENU_PAGE = "https://github.com/sponsors/davetcc";
    public static final String IO_EXPANDER_GUIDE_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/setting-up-io-expanders-in-menu-designer/";
    public static final String MENU_IN_MENU_GUIDE_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/java-menu-in-menu/";

    private final MenuEditorController controller;
    private final ArduinoLibraryInstaller installer;
    private final CodePluginManager pluginManager;
    private final CurrentProjectEditorUI editorUI;
    private final LibraryVersionDetector libraryVersionDetector;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ConfigurationStorage storage;
    private VBox libraryInfoVBox;
    private TextField appUuidLabel;
    private CheckBox recursiveNamingCheck;
    private CheckBox saveToSrcCheck;
    private CheckBox useCppMainCheck;
    private TextField appNameTextField;

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
        var options = controller.getProject().getGeneratorOptions();

        VBox vbox = new VBox();
        vbox.setSpacing(5);

        if (storage.getReleaseType() == ConfigurationStorage.TcMenuReleaseType.BETA) {
            Label docsLbl = new Label(" This is a BETA version - don't use in production");
            docsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
            vbox.getChildren().add(docsLbl);
        }

        GridPane gridPane = new GridPane();
        int row = 0;
        gridPane.setHgap(2);
        gridPane.setVgap(3);
        ColumnConstraints col1 = new ColumnConstraints(120);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints(100, 100, Double.MAX_VALUE);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        gridPane.add(new Label("Platform"), 0, row);
        var platformCombo = new ComboBox<>(observableArrayList(editorUI.getEmbeddedPlatforms()));
        platformCombo.setId("platformCombo");
        platformCombo.setMaxWidth(999);
        gridPane.add(platformCombo, 1, row, 2, 1);
        editorUI.getEmbeddedPlatforms().stream().filter(p -> p.getBoardId().equals(options.getEmbeddedPlatform()))
                .findFirst().ifPresent(env -> platformCombo.getSelectionModel().select(env));
        platformCombo.setOnAction(event -> {
            controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                    .withExisting(options)
                    .withPlatform(platformCombo.getSelectionModel().getSelectedItem().getBoardId())
                    .codeOptions());
        });
        ++row;

        gridPane.add(new Label("File name"), 0, row);
        TextField filenameField = new TextField(controller.getProject().getFileName());
        filenameField.setId("filenameField");
        filenameField.setEditable(false);
        gridPane.add(filenameField, 1, row, 2, 1);
        ++row;

        gridPane.add(new Label("Project unique ID"), 0, row);
        appUuidLabel = new TextField(options.getApplicationUUID().toString());
        appUuidLabel.setId("appUuidLabel");
        appUuidLabel.setEditable(false);
        gridPane.add(appUuidLabel, 1, row);
        Button changeId = new Button("Change ID");
        changeId.setId("changeIdBtn");
        changeId.setPrefWidth(99);
        changeId.setOnAction(e -> {
            if (editorUI.questionYesNo("Really change ID", "The ID is used by IoT devices to identify the device, so changing it may cause problems.")) {
                controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                        .withExisting(options)
                        .withNewId(UUID.randomUUID())
                        .codeOptions());
                appUuidLabel.setText(controller.getProject().getGeneratorOptions().getApplicationUUID().toString());
            }
        });
        gridPane.add(changeId, 2, row);
        ++row;

        gridPane.add(new Label("Project name"), 0, row);
        appNameTextField = new TextField(options.getApplicationName());
        appNameTextField.setId("appNameTextField");
        appNameTextField.textProperty().addListener((observable, oldValue, newValue) -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(options)
                .withAppName(newValue)
                .codeOptions()));
        gridPane.add(appNameTextField, 1, row, 2, 1);
        VBox.setMargin(gridPane, new Insets(10, 0, 10, 0));
        row++;
        gridPane.add(new Label("Project description"), 0, row);
        TextArea appDescTextArea = new TextArea(controller.getProject().getDescription());
        appDescTextArea.setId("appDescTextArea");
        appDescTextArea.setWrapText(true);
        appDescTextArea.setPrefRowCount(2);
        appDescTextArea.textProperty().addListener((observable, oldValue, newValue) -> controller.getProject().setDescription(newValue));
        gridPane.add(appDescTextArea, 1, row, 2, 1);
        ++row;

        recursiveNamingCheck = new CheckBox("Use fully qualified variable names for menu items");
        recursiveNamingCheck.setId("recursiveNamingCheck");
        recursiveNamingCheck.setSelected(options.isNamingRecursive());
        recursiveNamingCheck.setOnAction(e -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(controller.getProject().getGeneratorOptions())
                .withRecursiveNaming(recursiveNamingCheck.isSelected())
                .codeOptions()));
        gridPane.add(recursiveNamingCheck, 1, row++, 2, 1);

        saveToSrcCheck = new CheckBox("Save CPP and H files to src directory");
        saveToSrcCheck.setId("saveToSrcCheck");
        saveToSrcCheck.setSelected(options.isSaveToSrc());
        saveToSrcCheck.setOnAction(e -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(controller.getProject().getGeneratorOptions())
                .withSaveToSrc(saveToSrcCheck.isSelected())
                .codeOptions()));
        gridPane.add(saveToSrcCheck, 1, row++, 2, 1);

        useCppMainCheck = new CheckBox("Use CPP main instead of INO file");
        useCppMainCheck.setId("useCppMainCheck");
        useCppMainCheck.setSelected(options.isUseCppMain());
        useCppMainCheck.setOnAction(e -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(controller.getProject().getGeneratorOptions())
                .withCppMain(useCppMainCheck.isSelected())
                .codeOptions()));
        useCppMainCheck.setDisable(options.getEmbeddedPlatform().equals(EmbeddedPlatform.MBED_RTOS.getBoardId()));
        gridPane.add(useCppMainCheck, 1, row, 2, 1);

        vbox.getChildren().add(gridPane);

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
        executor.submit(() -> {
            if (libraryVersionDetector.availableVersionsAreValid(true)) {
                Platform.runLater(this::redrawTheTitlePage);
            }
        });
    }

    private void redrawTheTitlePage() {
        var vbox = libraryInfoVBox;
        vbox.getChildren().clear();
        boolean needRefresh = false;

        try {
            var currentUI = installer.getVersionOfLibrary("java-app", CURRENT_APP);
            if (!installer.getVersionOfLibrary("java-app", AVAILABLE_APP).equals(currentUI)) {
                var uiNeedsUpdate = new Button("There is a UI update available, you can check versions from General Settings");
                uiNeedsUpdate.setId("tcMenuStatusArea");
                uiNeedsUpdate.getStyleClass().add("libsNotOK");
                uiNeedsUpdate.setOnAction(actionEvent -> editorUI.showGeneralSettings());
                vbox.getChildren().add(uiNeedsUpdate);
                needRefresh = true;
            }

            if (installer.getArduinoDirectory().isEmpty() && storage.isUsingArduinoIDE()) {
                var setManually = new Button("Set Arduino directory in General Settings");
                setManually.setId("tcMenuStatusArea");
                setManually.getStyleClass().add("libsNotOK");
                vbox.getChildren().add(setManually);
                setManually.setOnAction(actionEvent -> editorUI.showGeneralSettings());
                needRefresh = true;
            } else if (storage.isUsingArduinoIDE() && installer.statusOfAllLibraries().isUpToDate()) {
                var lblTcMenuOK = new Label("Embedded Arduino libraries all up-to-date");
                lblTcMenuOK.setId("tcMenuStatusArea");
                lblTcMenuOK.getStyleClass().add("libsOK");
                vbox.getChildren().add(lblTcMenuOK);
            } else if (storage.isUsingArduinoIDE()) {
                var libsNotOK = new Button("Libraries need updating, check in General Settings");
                libsNotOK.getStyleClass().add("libsNotOK");
                libsNotOK.setId("tcMenuStatusArea");
                libsNotOK.setOnAction(actionEvent -> editorUI.showGeneralSettings());
                vbox.getChildren().add(libsNotOK);
                needRefresh = true;
            }

            boolean pluginsNotUpdated = (pluginManager.getLoadedPlugins().size() == 0);

            for (var plugin : pluginManager.getLoadedPlugins()) {
                var installedVersion = getVersionOfLibraryOrError(plugin, CURRENT_PLUGIN);
                pluginsNotUpdated = pluginsNotUpdated || !installedVersion.equals(currentUI);
            }

            if (!pluginManager.getLoadErrors().isEmpty()) {
                List<String> loadErrors = pluginManager.getLoadErrors();
                if (!loadErrors.isEmpty()) {
                    var errors = "At least one plugins failed, default plugins in ~/.tcmenu/plugins:\n"
                            + loadErrors.stream().limit(10).collect(Collectors.joining("\n"));

                    if (loadErrors.size() > 10) {
                        errors += "\nTruncated at 10 errors, please see logs..";
                    }

                    var pluginLabel = new Label(errors);
                    pluginLabel.setId("tcMenuPluginIndicator");
                    pluginLabel.getStyleClass().add("libsNotOK");
                    vbox.getChildren().add(pluginLabel);
                }
            } else if (pluginsNotUpdated) {
                var pluginLabel = new Button("SEVERE ERROR: Code generator plugins did not automatically update, click to retry");
                pluginLabel.setId("tcMenuPluginIndicator");
                pluginLabel.getStyleClass().add("libsNotOK");
                pluginLabel.setOnAction(actionEvent -> {
                    var alert = new Alert(Alert.AlertType.INFORMATION);
                    getJMetro().setScene(alert.getDialogPane().getScene());
                    alert.setTitle("Code Generator Plugin update");
                    alert.setHeaderText("Notes about plugins not updating");
                    alert.setContentText("When designer updated it could not update the plugins automatically. " +
                            "Before proceeding we recommend deleting ~/.tcmenu/plugins");
                    alert.showAndWait();
                    MenuEditorApp.createOrUpdateDirectoriesAsNeeded(storage);
                    controller.presentInfoPanel();
                });
                vbox.getChildren().add(pluginLabel);
                needRefresh = true;
            }

            if (needRefresh) {
                var refreshButton = new Button("Refresh library status");
                refreshButton.setOnAction(actionEvent -> controller.presentInfoPanel());
                vbox.getChildren().add(refreshButton);
            }
        } catch (IOException e) {
            vbox.getChildren().add(new Label("Error - " + e.getMessage()));
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
        docs.setOnAction((event) -> editorUI.browseToURL(urlToVisit));
        docs.setId(fxId);
        vbox.getChildren().add(docs);
    }

    public void focusFirst() {
        Platform.runLater(() -> appNameTextField.requestFocus());
    }
}
