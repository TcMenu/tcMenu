package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.cli.CreateProjectCommand;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.project.ProjectPersistor;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.thecoderscorner.menu.editorui.util.AlertUtil.showAlertAndWait;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

public class NewProjectController {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());
    public RadioButton newOnlyRadio;
    public RadioButton createNewRadio;
    public TextField projectNameField;
    public ComboBox<EmbeddedPlatform> platformCombo;
    public Button dirChooseButton;
    public Button createButton;
    public TextField locationTextField;
    public CheckBox cppMainCheckbox;
    public CheckBox enableI18nSupportCheck;
    public TextField namespaceField;
    private Optional<String> maybeDirectory;
    private CurrentEditorProject project;
    private ConfigurationStorage storage;
    private CodeGeneratorSupplier codeGeneratorSupplier;
    private final ResourceBundle bundle = MenuEditorApp.getBundle();
    private ProjectPersistor projectPersistor;
    private EmbeddedPlatforms platforms;

    public void initialise(ConfigurationStorage storage, CurrentEditorProject project, CodeGeneratorSupplier codeGeneratorSupplier,
                           ProjectPersistor projectPersistor, EmbeddedPlatforms platforms){
        this.storage = storage;
        this.codeGeneratorSupplier = codeGeneratorSupplier;
        maybeDirectory = storage.getArduinoOverrideDirectory();
        this.project = project;
        this.projectPersistor = projectPersistor;
        maybeDirectory.ifPresentOrElse(
                dir -> locationTextField.setText(dir),
                () -> locationTextField.setText("No directory")
        );

        platformCombo.setItems(FXCollections.observableList(platforms.getEmbeddedPlatforms()));
        platformCombo.getSelectionModel().select(0);
        this.platforms = platforms;
        reEvaluteAll();
    }

    public void onModeChange(ActionEvent actionEvent) {
        reEvaluteAll();
    }

    private void reEvaluteAll() {
        boolean newOnlyMode = newOnlyRadio.isSelected();
        projectNameField.setDisable(newOnlyMode);
        dirChooseButton.setDisable(newOnlyMode);
        platformCombo.setDisable(newOnlyMode);
        if(newOnlyMode) {
            cppMainCheckbox.setDisable(true);
            enableI18nSupportCheck.setDisable(true);
            createButton.setDisable(false);
        } else {
            var isJavaPlatform = platforms.isJava(platformCombo.getSelectionModel().getSelectedItem());
            var isNativeCpp = platforms.isNativeCpp(platformCombo.getSelectionModel().getSelectedItem());
            cppMainCheckbox.setDisable(isJavaPlatform || isNativeCpp);
            if (isNativeCpp) {
                cppMainCheckbox.setSelected(true);
            }
            enableI18nSupportCheck.setDisable(isJavaPlatform);
            var ok = (!StringHelper.isStringEmptyOrNull(projectNameField.getText()) && maybeDirectory.isPresent());
            if(isJavaPlatform && namespaceField.getText().isEmpty()) ok = false;
            createButton.setDisable(!ok);
        }
    }

    public void onDirectorySelection(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle(bundle.getString("create.project.choose.directory"));
        maybeDirectory.ifPresent(dir ->  directoryChooser.setInitialDirectory(new File(dir)));
        File possibleFile = directoryChooser.showDialog(createButton.getScene().getWindow());

        if(possibleFile != null && Files.exists(possibleFile.toPath())) {
            maybeDirectory = Optional.ofNullable(possibleFile.toString());
            locationTextField.setText(possibleFile.toString());
        }
    }

    public void onCancel(ActionEvent actionEvent) {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    public void onCreate(ActionEvent actionEvent) {
        boolean newOnlyMode = newOnlyRadio.isSelected();
        if(newOnlyMode) {
            if(!passDirtyCheck()) return;
            project.newProject();

        }
        else if(maybeDirectory.isPresent() && !StringHelper.isStringEmptyOrNull(projectNameField.getText())) {
            if(!passDirtyCheck()) return;
            try {
                String projName = projectNameField.getText();
                var projectCreator = new CreateProjectCommand();
                projectCreator.createNewProject(
                        Paths.get(maybeDirectory.get()), projName,
                        cppMainCheckbox.isSelected(),
                        platformCombo.getSelectionModel().getSelectedItem(),
                        s -> logger.log(INFO, s),
                        namespaceField.getText(),
                        codeGeneratorSupplier, projectPersistor
                );

                if(enableI18nSupportCheck.isSelected()) {
                    projectCreator.enableI18nSupport(Paths.get(maybeDirectory.get()).resolve(projName), List.of(Locale.FRENCH),
                            s -> logger.log(INFO, s), Optional.empty());
                }

                Path emfFileName = Paths.get(maybeDirectory.get(), projName, projName + ".emf");
                project.openProject(emfFileName.toString());
            } catch (Exception e) {
                logger.log(ERROR, "Failure processing create new project", e);
                showAlertAndWait(Alert.AlertType.ERROR, bundle.getString("create.dialog.error.creating"), ButtonType.CLOSE);
            }
        }
        else {
            showAlertAndWait(Alert.AlertType.WARNING, bundle.getString("create.dialog.fields.not.populated"), ButtonType.CLOSE);
            return; // avoid closing.
        }

        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private boolean passDirtyCheck() {
        if(project.isDirty()) {
            var btn = showAlertAndWait(Alert.AlertType.CONFIRMATION, bundle.getString("create.dialog.project.unsaved.header"),
                    bundle.getString("create.dialog.project.unsaved"), ButtonType.YES, ButtonType.NO);
            if(btn.orElse(ButtonType.NO) == ButtonType.YES) {
                project.setDirty(false);
                return true;
            }
            else return false;
        }
        return true;
    }

    public void onTextChanged(KeyEvent keyEvent) {
        reEvaluteAll();
    }

    public void onPlatformChanged(ActionEvent actionEvent) {
        reEvaluteAll();
    }
}
