package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.prefs.Preferences;

public class CodeGeneratorController {
    private static final String TARGET_KEY = "LastTarget";
    private static final String INPUT_KEY = "LastInput";
    private static final String DISPLAY_KEY = "LastDisplay";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public TextField directoryText;
    public ChoiceBox<DisplayType> displayCombo;
    public ChoiceBox<EmbeddedPlatform> targetCombo;
    public ChoiceBox<InputType> inputType;
    public Button generateButton;
    public Button cancelButton;
    public TextArea generatorLog;
    private CurrentEditorProject project;

    public void init(CurrentEditorProject project) {
        this.project = project;
        if(!project.isFileNameSet()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Project not yet saved");
            alert.setHeaderText("Project has not yet been saved");
            alert.setContentText("Before running the convert function, please ensure the project has been saved.");
            alert.showAndWait();
            throw new UnsupportedOperationException("Code not saved");
        }

        Path p = Paths.get(project.getFileName());
        Path folder = p.getParent();
        directoryText.setText(folder.toString());

        targetCombo.setItems(FXCollections.observableArrayList(EmbeddedPlatform.values()));
        displayCombo.setItems(FXCollections.observableArrayList(DisplayType.values()));
        inputType.setItems(FXCollections.observableArrayList(InputType.values()));

        // try and populate with the last settings if possible.
        try {
            Preferences prefs = Preferences.userNodeForPackage(getClass());
            EmbeddedPlatform platform = EmbeddedPlatform.valueOf(prefs.get(TARGET_KEY, EmbeddedPlatform.values()[0].name()));
            InputType input = InputType.valueOf(prefs.get(INPUT_KEY, InputType.values()[0].name()));
            DisplayType display = DisplayType.valueOf(prefs.get(DISPLAY_KEY, DisplayType.values()[0].name()));

            inputType.getSelectionModel().select(input);
            targetCombo.getSelectionModel().select(platform);
            displayCombo.getSelectionModel().select(display);
        }
        catch(Exception e) {
            inputType.getSelectionModel().selectFirst();
            targetCombo.getSelectionModel().selectFirst();
            displayCombo.getSelectionModel().selectFirst();
        }
    }

    public void onGenerateCode(ActionEvent event) {
        generatorLog.setText("Starting code generator run..");
        ArduinoGenerator generator = new ArduinoGenerator(
                this::genLogger,
                Paths.get(directoryText.getText()),
                displayCombo.getSelectionModel().getSelectedItem(),
                inputType.getSelectionModel().getSelectedItem(),
                project.getMenuTree()
        );
        if(generator.startConversion()) {
            generateButton.setDisable(true);
        }

        // serialise the last settings.
        Preferences prefs = Preferences.userNodeForPackage(getClass());
        prefs.put(DISPLAY_KEY, displayCombo.getSelectionModel().getSelectedItem().name());
        prefs.put(INPUT_KEY, inputType.getSelectionModel().getSelectedItem().name());
        prefs.put(TARGET_KEY, targetCombo.getSelectionModel().getSelectedItem().name());
    }

    void closeIt() {
        Stage s = (Stage) generatorLog.getScene().getWindow();
        s.close();
    }

    private void genLogger(String s) {
        Platform.runLater(()-> generatorLog.setText(generatorLog.getText() + "\n" + s));
    }

    public void onCancel(ActionEvent event) {
        closeIt();
    }
}
