/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.EnumWithApplicability;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;
import com.thecoderscorner.menu.editorui.project.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CodeGeneratorController {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    public ComboBox<InputType> inputTechCombo;
    public ComboBox<DisplayType> displayTechCombo;
    public ComboBox<EmbeddedPlatform> embeddedPlatformChoice;
    public ComboBox<RemoteCapabilities> remoteCapabilityCombo;
    public Label projectDirLabel;
    public TableView<CreatorProperty> propsTable;
    public TableColumn<CreatorProperty, String> defineCol;
    public TableColumn<CreatorProperty, String> typeCol;
    public TableColumn<CreatorProperty, String> valueCol;
    public TableColumn<CreatorProperty, String> descriptionCol;

    private CurrentEditorProject project;
    private CodeGeneratorDialog dialog;
    private List<CreatorProperty> properties = new ArrayList<>();

    private EmbeddedCodeCreator inputCreator;
    private EmbeddedCodeCreator displayCreator;
    private EmbeddedCodeCreator remoteCreator;

    public void init(CurrentEditorProject project, CodeGeneratorDialog dialog) {
        this.project = project;
        this.dialog = dialog;
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
        EmbeddedPlatform selPlatform = project.getGeneratorOptions().getEmbeddedPlatform();
        embeddedPlatformChoice.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> filterChoicesByPlatform(newVal));

        embeddedPlatformChoice.getSelectionModel().select(selPlatform);

        inputTechCombo.getSelectionModel().selectedItemProperty().addListener(this::inputTypeChanged);
        displayTechCombo.getSelectionModel().selectedItemProperty().addListener(this::displayTypeChanged);
        remoteCapabilityCombo.getSelectionModel().selectedItemProperty().addListener(this::remoteTypeChanged);

        inputTechCombo.getSelectionModel().select(project.getGeneratorOptions().getLastInputType());
        displayTechCombo.getSelectionModel().select(project.getGeneratorOptions().getLastDisplayType());
        remoteCapabilityCombo.getSelectionModel().select(project.getGeneratorOptions().getLastRemoteCapabilities());

        setUpTable();
    }

    private void setUpTable() {
        propsTable.setEditable(true);
        defineCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));
        typeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getSubsystem().toString()));
        valueCol.setCellValueFactory(param -> param.getValue().getProperty());
        valueCol.setEditable(true);
        valueCol.setCellFactory(TextFieldTableCell.forTableColumn());
        valueCol.setOnEditCommit(event -> event.getRowValue().getProperty().set(event.getNewValue()));
        descriptionCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getDescription()));
        }

    private void inputTypeChanged(Observable obs, InputType oldVal, InputType newVal) {
        inputCreator = newVal.makeCreator(project);
        changeProperties();
    }

    private void changeProperties() {
        List<EmbeddedCodeCreator> creators = Arrays.asList(displayCreator, inputCreator, remoteCreator);
        properties.clear();

        creators.stream()
                .filter(p -> p != null && p.properties().size() > 0)
                .forEach( creator -> {
                    setAllPropertiesToLastValues(creator.properties());
                    properties.addAll(creator.properties());
                });

        propsTable.setItems(FXCollections.observableArrayList(properties));
    }

    private void setAllPropertiesToLastValues(List<CreatorProperty> propsToDefault) {
        propsToDefault.forEach(prop -> project.getGeneratorOptions().getLastProperties().stream()
                .filter(p-> prop.getName().equals(p.getName()) && prop.getSubsystem().equals(p.getSubsystem()))
                .findFirst()
                .ifPresent(p-> prop.getProperty().set(p.getLatestValue())));
    }


    private void displayTypeChanged(Observable obs, DisplayType oldVal, DisplayType newVal) {
        displayCreator = newVal.makeCreator(project);
        changeProperties();
    }

    private void remoteTypeChanged(Observable obs, RemoteCapabilities oldVal, RemoteCapabilities newVal) {
        remoteCreator = newVal.makeCreator(project);
        changeProperties();
    }

    private <T extends EnumWithApplicability> void filterChoicesFor(ComboBox<T> choices, EmbeddedPlatform platform, Map<Integer, T> values) {
        SingleSelectionModel<T> selectionModel = choices.getSelectionModel();
        T sel = selectionModel.getSelectedItem();
        choices.setItems(FXCollections.observableArrayList(values.values().stream()
                .filter(inputType -> inputType.isApplicableFor(platform))
                .collect(Collectors.toList()))
        );
        if(sel != null) selectionModel.select(sel);
        if(selectionModel.getSelectedItem() == null) {
            selectionModel.selectFirst();
        }
    }

    private void filterChoicesByPlatform(EmbeddedPlatform selPlatform) {
        filterChoicesFor(inputTechCombo, selPlatform, InputType.values);
        inputTypeChanged(null, null, inputTechCombo.getValue());
        filterChoicesFor(displayTechCombo, selPlatform, DisplayType.values);
        displayTypeChanged(null, null, displayTechCombo.getValue());
        filterChoicesFor(remoteCapabilityCombo, selPlatform, RemoteCapabilities.values);
        remoteTypeChanged(null, null, remoteCapabilityCombo.getValue());
    }

    public void onGenerateCode(ActionEvent event) {
        dialog.startArduinoGenerator(
                Paths.get(project.getFileName()).getParent().toString(),
                Arrays.asList(inputCreator, displayCreator, remoteCreator)
        );
        closeIt();
    }

    void closeIt() {
        project.setGeneratorOptions(new CodeGeneratorOptions(
                embeddedPlatformChoice.getSelectionModel().getSelectedItem(),
                displayTechCombo.getSelectionModel().getSelectedItem(),
                inputTechCombo.getSelectionModel().getSelectedItem(),
                remoteCapabilityCombo.getSelectionModel().getSelectedItem(),
                properties
        ));
        Stage s = (Stage) embeddedPlatformChoice.getScene().getWindow();
        s.close();
    }

    public void onCancel(ActionEvent event) {
        closeIt();
    }

}
