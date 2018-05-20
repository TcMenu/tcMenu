/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeGeneratorController {
    private static final String TARGET_KEY = "LastTarget";
    private static final String INPUT_KEY = "LastInput";
    private static final String DISPLAY_KEY = "LastDisplay";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public ComboBox<EmbeddedPlatform> embeddedPlatformChoice;
    public TreeTableView componentTree;
    public TreeTableColumn propsColumn;
    public TreeTableColumn valuesColumn;
    private CurrentEditorProject project;

    public Label projectDirLabel;

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
        projectDirLabel.setText(folder.toString());

        embeddedPlatformChoice.setItems(FXCollections.observableArrayList(EmbeddedPlatform.values()));

    }

    public void onGenerateCode(ActionEvent event) {
        closeIt();
    }

    void closeIt() {
//        project.setCodeGeneratorOptions(new CodeGeneratorOptions(
//                embeddedPlatformChoice.getSelectionModel().getSelectedItem(),
//        ))
        Stage s = (Stage) embeddedPlatformChoice.getScene().getWindow();
        s.close();
    }

    public void onCancel(ActionEvent event) {
        closeIt();
    }

}
