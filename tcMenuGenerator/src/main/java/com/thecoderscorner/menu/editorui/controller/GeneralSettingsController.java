package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.PluginUpgradeTask;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.OnlineLibraryVersionDetector.ReleaseType;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller.InstallationType.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

@SuppressWarnings("unused")
public class GeneralSettingsController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public CheckBox usingArduinoLibsCheck;
    public CheckBox useFullyQualifiedNamesField;
    public CheckBox outputCppToSrcField;
    public TextField projectsTextField;
    public TextField libsTextField;
    public Button chooseArduinoButton;
    public Button chooseLibsButton;
    public ComboBox<ReleaseType> pluginStreamCombo;
    public TableColumn<NameWithVersion, String> libraryNameColumn;
    public TableColumn<NameWithVersion, String> expectedVerCol;
    public TableColumn<NameWithVersion, String> actualVerCol;
    public TableColumn<NameWithVersion, NameWithVersion> actionsCol;
    public Button updatePluginsBtn;
    public TableView<NameWithVersion> versionsTable;
    public ListView<String> additionalPathsList;
    public Button removePathBtn;
    private ConfigurationStorage storage;
    private String homeDirectory;
    private LibraryVersionDetector versionDetector;
    private PluginUpgradeTask upgrader;
    private CodePluginManager pluginManager;
    private ArduinoLibraryInstaller installer;

    public void initialise(ConfigurationStorage storage, LibraryVersionDetector versionDetector,
                           ArduinoLibraryInstaller installer, CodePluginManager pluginManager,
                           PluginUpgradeTask upgrader, String homeDirectory) {
        this.upgrader = upgrader;
        this.installer = installer;
        this.pluginManager = pluginManager;
        this.storage = storage;
        this.homeDirectory = homeDirectory;
        this.versionDetector = versionDetector;

        usingArduinoLibsCheck.setSelected(storage.isUsingArduinoIDE());
        useFullyQualifiedNamesField.setSelected(storage.isDefaultRecursiveNamingOn());
        outputCppToSrcField.setSelected(storage.isDefaultSaveToSrcOn());

        setDirectoryPickerOrEmpty(projectsTextField, storage.getArduinoOverrideDirectory(), () -> {
            var ardDir = getArduinoDirectory();
            if(ardDir.isPresent()) {
                storage.setArduinoOverrideDirectory(ardDir.get().toString());
                return Optional.of(ardDir.get().toString());
            }
            return Optional.empty();
        });
        setDirectoryPickerOrEmpty(libsTextField, storage.getArduinoLibrariesOverrideDirectory(), () -> {
            var ardDir = getArduinoLibsDirectory();
            if(ardDir.isPresent()) {
                storage.setArduinoLibrariesOverrideDirectory(ardDir.get().toString());
                return Optional.of(ardDir.get().toString());
            }
            return Optional.empty();
        });

        libraryNameColumn.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().name()));
        expectedVerCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().available().toString()));
        actualVerCol.setCellValueFactory(cell -> new ReadOnlyObjectWrapper<>(cell.getValue().installed().toString()));
        actionsCol.setCellValueFactory(new NameWithVersionValueFactory());

        pluginStreamCombo.setItems(FXCollections.observableList(Arrays.asList(ReleaseType.values())));
        pluginStreamCombo.getSelectionModel().select(versionDetector.getReleaseType());

        additionalPathsList.getSelectionModel().selectedItemProperty().addListener((observableValue, s1, s2) ->
                removePathBtn.setDisable(additionalPathsList.getSelectionModel().getSelectedItem() == null));

        List<String> additionalPaths = storage.getAdditionalPluginPaths();
        additionalPathsList.setItems(FXCollections.observableList(additionalPaths));
        if(!additionalPaths.isEmpty()) additionalPathsList.getSelectionModel().select(0);

        populateVersions();
    }

    private void setDirectoryPickerOrEmpty(TextField field, Optional<String> maybePath, Supplier<Optional<String>> defaulter) {
        if(maybePath.isPresent()) {
            field.setText(maybePath.get());
        }
        else {
            var maybeDefaultPath = defaulter.get();
            if(maybeDefaultPath.isPresent()) {
                field.setText(maybeDefaultPath.get());
            }
            else {
                field.setText("Path not yet set");
            }
        }
    }

    public void onChooseArduino(ActionEvent actionEvent) {
        var maybeSelectedFile = directoryNameFromUser(getArduinoDirectory(), true);
        maybeSelectedFile.ifPresent(selected -> {
            storage.setArduinoOverrideDirectory(selected);
            projectsTextField.setText(selected);

            var possibleLibPath = Paths.get(selected, "libraries");
            if(Files.exists(possibleLibPath)) {
                storage.setArduinoLibrariesOverrideDirectory(possibleLibPath.toString());
                libsTextField.setText(possibleLibPath.toString());
            }
        });
    }

    public void onChooseLibs(ActionEvent actionEvent) {
        var maybeSelectedFile = directoryNameFromUser(getArduinoLibsDirectory(), true);
        maybeSelectedFile.ifPresent(selected -> {
            storage.setArduinoLibrariesOverrideDirectory(selected);
            libsTextField.setText(selected);
        });
    }

    public void onCheckboxChanged(ActionEvent actionEvent) {
        var usingLibs = usingArduinoLibsCheck.isSelected();
        storage.setUsingArduinoIDE(usingLibs);
        storage.setDefaultRecursiveNamingOn(useFullyQualifiedNamesField.isSelected());
        storage.setDefaultSaveToSrcOn(outputCppToSrcField.isSelected());

        chooseArduinoButton.setDisable(!usingLibs);
        chooseLibsButton.setDisable(!usingLibs);
    }

    public Optional<Path> getArduinoLibsDirectory() {
        var maybeArdDir = getArduinoDirectory();
        if(maybeArdDir.isEmpty()) return Optional.empty();

        var libs = maybeArdDir.get().resolve("libraries");
        if(Files.exists(libs)) {
            return Optional.of(libs);
        }

        return Optional.empty();
    }

    public Optional<Path> getArduinoDirectory() {
        logger.log(System.Logger.Level.INFO, "Looking for Arduino directory");

        Path arduinoPath = Paths.get(homeDirectory, "Documents/Arduino");
        if (!Files.exists(arduinoPath)) {
            logger.log(System.Logger.Level.INFO, "Not found in " + arduinoPath);
            // On Linux, Arduino directory defaults to home.
            arduinoPath = Paths.get(homeDirectory, "Arduino");
        }
        if (!Files.exists(arduinoPath)) {
            logger.log(System.Logger.Level.INFO, "Not found in " + arduinoPath);
            // try again in the onedrive folder, noticed it there on several windows machines
            arduinoPath = Paths.get(homeDirectory, "OneDrive/Documents/Arduino");
        }
        if (!Files.exists(arduinoPath)) {
            logger.log(System.Logger.Level.INFO, "Not found in " + arduinoPath);
        }

        if (!Files.exists(arduinoPath)) return Optional.empty();
        logger.log(System.Logger.Level.INFO, "Arduino directory found at " + arduinoPath);

        logger.log(System.Logger.Level.INFO, "looking for libraries");

        // there was an arduino install without a libraries directory - add it.
        Path libsPath = arduinoPath.resolve("libraries");
        if (!Files.exists(libsPath)) {
            try {
                logger.log(System.Logger.Level.INFO, "Creating libraries folder");
                Files.createDirectory(libsPath);
            } catch (IOException e) {
                return Optional.empty();
            }
        }

        return Optional.of(arduinoPath);
    }

    public Optional<String> directoryNameFromUser(Optional<Path> initialDir, boolean open) {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Choose Directory");
        initialDir.ifPresent(dir -> dirChooser.setInitialDirectory(new File(dir.toString())));

        File f = dirChooser.showDialog(libsTextField.getScene().getWindow());

        if (f != null) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) libsTextField.getScene().getWindow();
        s.close();
    }

    public void onStreamChanged(ActionEvent actionEvent) {
        var newStream = pluginStreamCombo.getSelectionModel().getSelectedItem();
        if(newStream != null) {
            versionDetector.changeReleaseType(newStream);
            onRefreshLibraries(actionEvent);
        }
    }

    public void onUpdatePlugins(ActionEvent actionEvent) {
        upgrader.startUpdateProcedure(updatePluginsBtn);
        populateVersions();
    }

    public void onRefreshLibraries(ActionEvent actionEvent) {
        populateVersions();
    }

    private void populateVersions() {
        var fr = executor.submit(() -> {
            versionDetector.acquireVersions();
            Platform.runLater(this::populateListAfterRequest);
        });
    }

    private void populateListAfterRequest() {
        boolean pluginUpdateNeeded = false;
        versionsTable.getItems().clear();

        try {
            logger.log(INFO, "Start plugin version detection");

            for(var plugin : pluginManager.getLoadedPlugins()) {
                var availableVersion = getVersionOfLibraryOrError(plugin.getModuleName(), AVAILABLE_PLUGIN);
                var installedVersion = getVersionOfLibraryOrError(plugin.getModuleName(), CURRENT_PLUGIN);
                pluginUpdateNeeded = pluginUpdateNeeded || !installedVersion.equals(availableVersion);

                var ver = new NameWithVersion(plugin.getModuleName() + " plugin", plugin.getModuleName(), true, availableVersion, installedVersion);
                versionsTable.getItems().add(ver);
            }
            logger.log(INFO, "Start library version detection");

            versionsTable.getItems().add(findLibVersion("tcMenu"));
            versionsTable.getItems().add(findLibVersion("IoAbstraction"));
            versionsTable.getItems().add(findLibVersion("LiquidCrystalIO"));
            versionsTable.getItems().add(findLibVersion("TaskManagerIO"));

            versionsTable.getItems().add(new NameWithVersion(
                    "TcMenuDesigner UI", "tcMenuDesigner",
                    false,
                    getVersionOfLibraryOrError("java-app", AVAILABLE_APP),
                    getVersionOfLibraryOrError("java-app", CURRENT_APP)
            ));

            logger.log(INFO, "Done with version detection, setting cell factory and returning");

            actionsCol.setCellFactory((tableColumn) -> new TableCell<>() {

                @Override
                protected void updateItem(NameWithVersion item, boolean empty) {
                    super.updateItem(item, empty);

                    this.setGraphic(null);
                    this.setText(null);

                    if(item != null && item.isPlugin()) {
                        var versions = versionDetector.acquireAllVersionsFor(item.underlyingId());

                        if (versions.isPresent()) {
                            List<String> list = versions.get().stream()
                                    .map(ver -> ver.toString() + ((ver.equals(item.available) ? " *" : "")))
                                    .collect(Collectors.toList());

                            var cbx = new ComboBox<>(FXCollections.observableList(list));
                            cbx.getSelectionModel().select(item.available + " *");
                            var container = new HBox(4);
                            container.getChildren().add(cbx);
                            container.getChildren().add(new Button(">>"));
                            this.setGraphic(container);
                            this.setText(null);
                        }
                    }
                }
            });

            updatePluginsBtn.setDisable(!pluginUpdateNeeded);
        } catch (IOException e) {
            logger.log(ERROR, "Unable to load plugin and lib versions", e);
        }
    }

    private NameWithVersion findLibVersion(String libName) throws IOException {
        var available = installer.getVersionOfLibrary(libName, AVAILABLE_LIB);
        var installed = installer.getVersionOfLibrary(libName, CURRENT_LIB);

        if(available == null) available = VersionInfo.ERROR_VERSION;
        if(installed == null) installed = VersionInfo.ERROR_VERSION;

        return new NameWithVersion(libName + " library", libName, false, available, installed);
    }

    private VersionInfo getVersionOfLibraryOrError(String name, ArduinoLibraryInstaller.InstallationType type) {
        try {
            var version = installer.getVersionOfLibrary(name, type);
            if(version == null) version = VersionInfo.ERROR_VERSION;
            return version;
        } catch (IOException e) {
            return VersionInfo.ERROR_VERSION;
        }
    }

    public void onAddNewPath(ActionEvent actionEvent) {
        var maybeDir = directoryNameFromUser(getArduinoDirectory(), true);
        maybeDir.ifPresent(dir -> {
            var paths = new ArrayList<>(storage.getAdditionalPluginPaths());
            paths.add(dir);
            storage.setAdditionalPluginPaths(paths);
            refreshAdditionalPaths(paths);
        });
    }

    private void refreshAdditionalPaths(ArrayList<String> paths) {
        additionalPathsList.setItems(FXCollections.observableList(paths));
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Reload all plugins?", ButtonType.YES, ButtonType.NO);
        alert.setTitle("Plugin reload");
        var result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.YES) {
            pluginManager.reload();
        }
    }

    public void onRemoveSelectedPath(ActionEvent actionEvent) {
        var sel = additionalPathsList.getSelectionModel().getSelectedItem();
        if(sel == null) return;
        var paths = new ArrayList<>(storage.getAdditionalPluginPaths());
        paths.remove(sel);
        storage.setAdditionalPluginPaths(paths);
        refreshAdditionalPaths(paths);
    }

    public record NameWithVersion(String name, String underlyingId, boolean isPlugin, VersionInfo available, VersionInfo installed) { }

    static class NameWithVersionValueFactory implements Callback<TableColumn.CellDataFeatures<NameWithVersion, NameWithVersion>, ObservableValue<NameWithVersion>> {
        @Override
        public ObservableValue<NameWithVersion> call(TableColumn.CellDataFeatures<NameWithVersion, NameWithVersion> data) {
            return new ReadOnlyObjectWrapper<>(data.getValue());
        }
    }
}
