/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.SubSystem.*;

public class GenerateCodeDialog {
    private final CodePluginManager manager;
    private final CurrentProjectEditorUI editorUI;
    private final CurrentEditorProject project;
    private final CodeGeneratorRunner runner;

    private List<CodePluginItem> displaysSupported;
    private List<CodePluginItem> inputsSupported;
    private List<CodePluginItem> remotesSupported;

    private UICodePluginItem currentDisplay;
    private UICodePluginItem currentInput;
    private UICodePluginItem currentRemote;

    private ComboBox<EmbeddedPlatform> platformCombo;

    public GenerateCodeDialog(CodePluginManager manager,  CurrentProjectEditorUI editorUI,
                              CurrentEditorProject project, CodeGeneratorRunner runner) {

        this.manager = manager;
        this.editorUI = editorUI;
        this.project = project;
        this.runner = runner;

        displaysSupported = manager.getPluginsThatMatch(project.getGeneratorOptions().getEmbeddedPlatform(), DISPLAY);
        inputsSupported = manager.getPluginsThatMatch(project.getGeneratorOptions().getEmbeddedPlatform(), INPUT);
        remotesSupported = manager.getPluginsThatMatch(project.getGeneratorOptions().getEmbeddedPlatform(), REMOTE);

    }

    public void showCodeGenerator(Stage stage, boolean modal) {

        VBox vbox = new VBox(5);
        vbox.setPrefSize(640, 500);

        placeDirectoryAndEmbeddedPanels(vbox);

        BorderPane pane = new BorderPane();
        Label titleLbl = new Label("Input device:");
        titleLbl.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        pane.setLeft(titleLbl);
        pane.setRight(new Button("Change input"));
        vbox.getChildren().add(pane);

        CodePluginItem itemInput = findItemByUuidOrDefault(inputsSupported, project.getGeneratorOptions().getLastInputUuid());
        currentInput = new UICodePluginItem(manager, itemInput);
        vbox.getChildren().add(currentInput);


        pane = new BorderPane();
        Label titleLbl1 = new Label("Display device:");
        titleLbl1.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        pane.setLeft(titleLbl1);
        pane.setRight(new Button("Change display"));
        vbox.getChildren().add(pane);

        CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, project.getGeneratorOptions().getLastDisplayUuid());
        currentDisplay = new UICodePluginItem(manager, itemDisplay);
        vbox.getChildren().add(currentDisplay);

        pane = new BorderPane();
        Label titleLbl2 = new Label("Remote device:");
        titleLbl2.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        pane.setLeft(titleLbl2);
        pane.setRight(new Button("Change remote"));
        vbox.getChildren().add(pane);

        CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, project.getGeneratorOptions().getLastRemoteCapabilitiesUuid());
        currentRemote = new UICodePluginItem(manager, itemRemote);
        vbox.getChildren().add(currentRemote);

        BorderPane root = new BorderPane();
        root.setTop(vbox);
        root.setPadding(new Insets(5));

        Stage dialogStage = new Stage();
        dialogStage.setTitle("Code Generator");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);
        Scene scene = new Scene(root);
        dialogStage.setScene(scene);
        if(modal) {
            dialogStage.showAndWait();
        }
        else {
            dialogStage.show();
        }

    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid) {
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElse(items.get(0));
    }

    private void placeDirectoryAndEmbeddedPanels(VBox vbox) {
        BorderPane directoryPane = new BorderPane();
        directoryPane.setLeft(new Label("Project directory"));
        directoryPane.setRight(new Label(project.getFileName()));
        vbox.getChildren().add(directoryPane);

        BorderPane embeddedPane = new BorderPane();
        embeddedPane.setLeft(new Label("Embedded Platform"));
        platformCombo = new ComboBox<>(FXCollections.observableArrayList(EmbeddedPlatform.values()));
        embeddedPane.setRight(platformCombo);
        vbox.getChildren().add(embeddedPane);
        platformCombo.getSelectionModel().select(project.getGeneratorOptions().getEmbeddedPlatform());
        platformCombo.getSelectionModel().selectedItemProperty().addListener((observable, oldVal, newVal) -> filterChoicesByPlatform(newVal));
    }

    private void filterChoicesByPlatform(EmbeddedPlatform newVal) {
        displaysSupported = manager.getPluginsThatMatch(newVal, DISPLAY);
        inputsSupported = manager.getPluginsThatMatch(newVal, INPUT);
    }
}
