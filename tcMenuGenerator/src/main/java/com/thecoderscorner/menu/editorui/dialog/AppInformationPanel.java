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
import com.thecoderscorner.menu.editorui.generator.ProjectSaveLocation;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.persist.ReleaseType;
import com.thecoderscorner.menu.persist.VersionInfo;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;
import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;
import static javafx.collections.FXCollections.observableArrayList;

public class AppInformationPanel {
    public static final String LIBRARY_DOCS_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/";
    public static final String GITHUB_PROJECT_URL = "https://github.com/davetcc/tcMenu/";
    public static final String GITHUB_LANGUAGE_FILES_URL = GITHUB_PROJECT_URL + "blob/master/tcMenuGenerator/scripts/i18n-readme.md";
    public static final String GITHUB_DISCUSSION_URL = GITHUB_PROJECT_URL + "discussions";
    public static final String GETTING_STARTED_PAGE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-overview-quick-start/";
    public static final String FONTS_GUIDE_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/using-custom-fonts-in-menu/";
    public static final String TCC_FORUM_PAGE = "https://www.thecoderscorner.com/jforum/recentTopics/list.page";
    public static final String EEPROM_HELP_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/menu-eeprom-integrations/";
    public static final String AUTHENTICATOR_HELP_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/secure-menuitem-pins-and-remotes/";
    public static final String SPONSOR_TCMENU_PAGE = "https://github.com/sponsors/davetcc";
    public static final String BUY_ME_A_COFFEE_URL = "https://www.buymeacoffee.com/davetcc";
    public static final String IO_EXPANDER_GUIDE_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/setting-up-io-expanders-in-menu-designer/";
    public static final String MENU_IN_MENU_GUIDE_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-iot/java-menu-in-menu/";
    public static final String CREATE_USE_BITMAP_PAGE = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/creating-and-using-bitmaps-menu/";

    private final PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();
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
    private ComboBox<ProjectSaveLocation> saveToSrcCombo;
    private CheckBox useCppMainCheck;
    private CheckBox useSizedEepromStorage;
    private TextField appNameTextField;
    private final AtomicBoolean saveToSrcRecurseProtect = new AtomicBoolean(false);
    private final ResourceBundle bundle = MenuEditorApp.getBundle();

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

        if (storage.getReleaseType() == ReleaseType.BETA) {
            Label docsLbl = new Label(" " + bundle.getString("core.beta.version.warning"));
            docsLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
            vbox.getChildren().add(docsLbl);
        }

        GridPane gridPane = new GridPane();
        int row = 0;
        gridPane.setHgap(2);
        gridPane.setVgap(3);
        ColumnConstraints col1 = new ColumnConstraints(120, 250, Double.MAX_VALUE);
        col1.setHgrow(Priority.SOMETIMES);
        ColumnConstraints col2 = new ColumnConstraints(300, 300, Double.MAX_VALUE);
        col2.setHgrow(Priority.ALWAYS);
        ColumnConstraints col3 = new ColumnConstraints();
        col3.setHgrow(Priority.SOMETIMES);
        col3.setPercentWidth(15);
        gridPane.getColumnConstraints().addAll(col1, col2, col3);

        gridPane.add(new Label(bundle.getString("core.platform")), 0, row);
        var platformCombo = new ComboBox<>(observableArrayList(editorUI.getEmbeddedPlatforms()));
        platformCombo.setId("platformCombo");
        platformCombo.setMaxWidth(99999);
        gridPane.add(platformCombo, 1, row, 2, 1);
        platformCombo.getSelectionModel().select(options.getEmbeddedPlatform());
        platformCombo.setOnAction(event -> {
            controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                    .withExisting(options)
                    .withPlatform(platformCombo.getSelectionModel().getSelectedItem())
                    .codeOptions());
            useCppMainCheck.setDisable(platforms.isNativeCpp(platformCombo.getValue()) || platforms.isJava(platformCombo.getValue()));
            useSizedEepromStorage.setDisable(platforms.isJava(platformCombo.getValue()));
        });
        ++row;

        gridPane.add(new Label(bundle.getString("core.file.name")), 0, row);
        TextField filenameField = new TextField(controller.getProject().getFileName());
        filenameField.setId("filenameField");
        filenameField.setEditable(false);
        gridPane.add(filenameField, 1, row, 2, 1);
        ++row;

        gridPane.add(new Label(bundle.getString("core.project.uuid")), 0, row);
        appUuidLabel = new TextField(options.getApplicationUUID().toString());
        appUuidLabel.setId("appUuidLabel");
        appUuidLabel.setEditable(false);
        gridPane.add(appUuidLabel, 1, row);
        Button changeId = new Button(bundle.getString("app.info.change.id"));
        changeId.setId("changeIdBtn");
        changeId.setMaxWidth(99999);
        changeId.setOnAction(e -> {
            if (editorUI.questionYesNo(bundle.getString("app.info.really.change.id.title"), bundle.getString("app.info.really.change.id.message"))) {
                controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                        .withExisting(options)
                        .withNewId(UUID.randomUUID())
                        .codeOptions());
                appUuidLabel.setText(controller.getProject().getGeneratorOptions().getApplicationUUID().toString());
            }
        });
        gridPane.add(changeId, 2, row);
        ++row;

        gridPane.add(new Label(bundle.getString("app.info.project.name")), 0, row);
        appNameTextField = new TextField(options.getApplicationName());
        appNameTextField.setId("appNameTextField");
        appNameTextField.textProperty().addListener((observable, oldValue, newValue) -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(options)
                .withAppName(newValue)
                .codeOptions()));
        gridPane.add(appNameTextField, 1, row, 2, 1);
        VBox.setMargin(gridPane, new Insets(10, 0, 10, 0));
        row++;
        gridPane.add(new Label(bundle.getString("app.info.project.description")), 0, row);
        TextArea appDescTextArea = new TextArea(controller.getProject().getDescription());
        appDescTextArea.setId("appDescTextArea");
        appDescTextArea.setWrapText(true);
        appDescTextArea.setPrefRowCount(2);
        appDescTextArea.textProperty().addListener((observable, oldValue, newValue) -> controller.getProject().setDescription(newValue));
        gridPane.add(appDescTextArea, 1, row, 2, 1);
        ++row;

        gridPane.add(new Label(bundle.getString("app.info.save.gen.loc")), 0, row);
        saveToSrcCombo = new ComboBox<>();
        saveToSrcCombo.setId("saveToSrcCbx");
        saveToSrcCombo.setItems(FXCollections.observableArrayList(ProjectSaveLocation.values()));
        saveToSrcCombo.getSelectionModel().select(options.getSaveLocation());
        saveToSrcCombo.setMaxWidth(999999);
        saveToSrcCombo.setOnAction(e -> Platform.runLater(this::saveToSrcPressed));
        gridPane.add(saveToSrcCombo, 1, row++, 2, 1);

        recursiveNamingCheck = new CheckBox(bundle.getString("app.info.check.use.recursive.naming"));
        recursiveNamingCheck.setId("recursiveNamingCheck");
        recursiveNamingCheck.setSelected(options.isNamingRecursive());
        recursiveNamingCheck.setOnAction(e -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(controller.getProject().getGeneratorOptions())
                .withRecursiveNaming(recursiveNamingCheck.isSelected())
                .codeOptions()));
        gridPane.add(recursiveNamingCheck, 1, row++, 2, 1);

        useCppMainCheck = new CheckBox(bundle.getString("app.info.check.use.cpp.main"));
        useCppMainCheck.setId("useCppMainCheck");
        useCppMainCheck.setSelected(options.isUseCppMain());
        useCppMainCheck.setOnAction(e -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                .withExisting(controller.getProject().getGeneratorOptions())
                .withCppMain(useCppMainCheck.isSelected())
                .codeOptions()));
        useCppMainCheck.setDisable(platforms.isNativeCpp(platformCombo.getValue()) || platforms.isJava(platformCombo.getValue()));
        gridPane.add(useCppMainCheck, 1, row++, 2, 1);

        useSizedEepromStorage = new CheckBox(bundle.getString("app.info.check.use.size.based.eeprom"));
        useSizedEepromStorage.setId("useSizedEepromStorage");
        useSizedEepromStorage.setSelected(options.isUsingSizedEEPROMStorage());
        useSizedEepromStorage.setTooltip(new Tooltip("Save the largest EEPROM location to prevent unsaved new items with higher locations loading"));
        useSizedEepromStorage.setOnAction(event -> controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                        .withExisting(controller.getProject().getGeneratorOptions())
                        .withUseSizedEEPROMStorage(useSizedEepromStorage.isSelected())
                        .codeOptions()));
        useCppMainCheck.setDisable(platforms.isNativeCpp(platformCombo.getValue()) || platforms.isJava(platformCombo.getValue()));
        gridPane.add(useSizedEepromStorage, 1, row, 2, 1);

        vbox.getChildren().add(gridPane);

        // add the documentation links
        labelWithUrl(vbox, LIBRARY_DOCS_URL, bundle.getString("app.info.browse.link.docs"), "libdocsurl");
        labelWithUrl(vbox, GITHUB_PROJECT_URL, bundle.getString("app.info.give.us.a.star"), "githuburl");

        libraryInfoVBox = new VBox(3.0);
        checkAndReportItems(libraryInfoVBox);
        vbox.getChildren().add(libraryInfoVBox);
        return vbox;
    }

    private void checkAndReportItems(VBox vbox) {
        vbox.getChildren().clear();
        vbox.getChildren().add(new Label(bundle.getString("app.info.reading.version.info")));
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
            if (!currentUI.isSameOrNewerThan(installer.getVersionOfLibrary("java-app", AVAILABLE_APP))) {
                var uiNeedsUpdate = new Button(bundle.getString("app.info.ui.update.available"));
                uiNeedsUpdate.setId("tcMenuStatusArea");
                uiNeedsUpdate.getStyleClass().add("libsNotOK");
                uiNeedsUpdate.setOnAction(actionEvent -> editorUI.showGeneralSettings());
                vbox.getChildren().add(uiNeedsUpdate);
                needRefresh = true;
            }

            if (installer.getArduinoDirectory().isEmpty() && storage.isUsingArduinoIDE()) {
                var setManually = new Button(bundle.getString("app.info.ui.set.arduino.directory"));
                setManually.setId("tcMenuStatusArea");
                setManually.getStyleClass().add("libsNotOK");
                vbox.getChildren().add(setManually);
                setManually.setOnAction(actionEvent -> editorUI.showGeneralSettings());
                needRefresh = true;
            } else if (storage.isUsingArduinoIDE() && installer.areCoreLibrariesUpToDate()) {
                var lblTcMenuOK = new Label(bundle.getString("app.info.ui.libraries.up.to.date"));
                lblTcMenuOK.setId("tcMenuStatusArea");
                lblTcMenuOK.getStyleClass().add("libsOK");
                vbox.getChildren().add(lblTcMenuOK);
            } else if (storage.isUsingArduinoIDE()) {
                var libsNotOK = new Button(bundle.getString("app.info.ui.libraries.need.update"));
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
                    var errors = bundle.getString("app.info.plugin.load.failure") + " ~/.tcmenu/plugins:\n"
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
                var pluginLabel = new Button(bundle.getString("app.info.severe.error"));
                pluginLabel.setId("tcMenuPluginIndicator");
                pluginLabel.getStyleClass().add("libsNotOK");
                pluginLabel.setOnAction(actionEvent -> {
                    showAlertAndWait(Alert.AlertType.INFORMATION, bundle.getString("app.info.plugin.updater.fail.header")
                            , bundle.getString("app.info.plugin.updater.fail.message"), ButtonType.CLOSE);
                    controller.presentInfoPanel();
                });
                vbox.getChildren().add(pluginLabel);
                needRefresh = true;
            }

            if (needRefresh) {
                var refreshButton = new Button(bundle.getString("app.info.plugin.refresh"));
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

    private void saveToSrcPressed() {
        if(saveToSrcRecurseProtect.get()) return;

        // when the project is new (never saved), we are safe to change the save location type.
        if(!controller.getProject().isFileNameSet()) {
            controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                    .withExisting(controller.getProject().getGeneratorOptions())
                    .withSaveLocation(saveToSrcCombo.getSelectionModel().getSelectedItem())
                    .codeOptions());
            return;
        }

        var location = Paths.get(controller.getProject().getFileName()).toFile().getParentFile().toPath();

        var isNotSameAsCurrent = controller.getProject().getGeneratorOptions().getSaveLocation() != saveToSrcCombo.getSelectionModel().getSelectedItem();
        if(isNotSameAsCurrent && editorUI.questionYesNo(bundle.getString("app.info.change.src.dir.header"),
                bundle.getString("app.info.change.src.dir.message") + " - " + location)) {
            controller.getProject().setGeneratorOptions(new CodeGeneratorOptionsBuilder()
                    .withExisting(controller.getProject().getGeneratorOptions())
                    .withSaveLocation(saveToSrcCombo.getSelectionModel().getSelectedItem())
                    .codeOptions());
        } else {
            saveToSrcCombo.getSelectionModel().select(controller.getProject().getGeneratorOptions().getSaveLocation());
        }
    }
}
