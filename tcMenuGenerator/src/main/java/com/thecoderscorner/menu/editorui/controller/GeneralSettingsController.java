package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.LibraryVersionDetector;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginConfig;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.util.VersionInfo;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.PluginUpgradeTask;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

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
    public Button updatePluginsBtn;
    public TableView<NameWithVersion> versionsTable;
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

        libraryNameColumn.setCellValueFactory(cell -> new ImmutableObservableValue(cell.getValue().name()));
        expectedVerCol.setCellValueFactory(cell -> new ImmutableObservableValue(cell.getValue().available()));
        actualVerCol.setCellValueFactory(cell -> new ImmutableObservableValue(cell.getValue().installed()));

        pluginStreamCombo.setItems(FXCollections.observableList(Arrays.asList(ReleaseType.values())));
        pluginStreamCombo.getSelectionModel().select(versionDetector.getReleaseType());

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

                var ver = new NameWithVersion(plugin.getModuleName() + " plugin", availableVersion, installedVersion);
                versionsTable.getItems().add(ver);
            }
            logger.log(INFO, "Start library version detection");

            versionsTable.getItems().add(findLibVersion("tcMenu"));
            versionsTable.getItems().add(findLibVersion("IoAbstraction"));
            versionsTable.getItems().add(findLibVersion("LiquidCrystalIO"));
            versionsTable.getItems().add(findLibVersion("TaskManagerIO"));

            versionsTable.getItems().add(new NameWithVersion(
                    "TcMenuDesigner UI",
                    getVersionOfLibraryOrError("java-app", AVAILABLE_APP),
                    getVersionOfLibraryOrError("java-app", CURRENT_APP)
            ));

            logger.log(INFO, "Done with version detection");

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

        return new NameWithVersion(libName + " library", available, installed);
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

    public record NameWithVersion(String name, VersionInfo available, VersionInfo installed) { }

    public record ImmutableObservableValue(Object dataItem) implements ObservableValue<String> {

        //
        // the data never changes
        //
        @Override
        public void addListener(ChangeListener<? super String> changeListener) {
        }

        @Override
        public void removeListener(ChangeListener<? super String> changeListener) {
        }

        @Override
        public void addListener(InvalidationListener invalidationListener) {
        }

        @Override
        public void removeListener(InvalidationListener invalidationListener) {
        }

        @Override
        public String getValue() {
            if(dataItem == null) return "<NULL>";
            return dataItem.toString();
        }
    }
}
