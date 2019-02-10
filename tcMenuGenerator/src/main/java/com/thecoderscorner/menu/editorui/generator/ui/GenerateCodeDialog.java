/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.project.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.CHANGE;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.SELECT;
import static com.thecoderscorner.menu.pluginapi.SubSystem.*;
import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;
import static javafx.collections.FXCollections.observableArrayList;

public class GenerateCodeDialog {
    private final System.Logger logger = System.getLogger(getClass().getSimpleName());

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
    private EmbeddedCodeCreator displayCreator;
    private EmbeddedCodeCreator inputCreator;
    private EmbeddedCodeCreator remoteCreator;

    private ComboBox<EmbeddedPlatform> platformCombo;
    private Button generateButton;
    private Button cancelButton;
    private Stage mainStage;

    public TableView<CreatorProperty> propsTable;
    public TableColumn<CreatorProperty, String> defineCol;
    public TableColumn<CreatorProperty, String> typeCol;
    public TableColumn<CreatorProperty, String> valueCol;
    public TableColumn<CreatorProperty, String> descriptionCol;
    private List<CreatorProperty> properties = new ArrayList<>();
    private Stage dialogStage;


    public GenerateCodeDialog(CodePluginManager manager,  CurrentProjectEditorUI editorUI,
                              CurrentEditorProject project, CodeGeneratorRunner runner) {
        this.manager = manager;
        this.editorUI = editorUI;
        this.project = project;
        this.runner = runner;
    }

    public void showCodeGenerator(Stage stage, boolean modal)  {
        this.mainStage = stage;
        VBox vbox = new VBox(5);

        placeDirectoryAndEmbeddedPanels(vbox);
        filterChoicesByPlatform(platformCombo.getValue());

        addTitleLabel(vbox, "Select the input type:");
        CodeGeneratorOptions genOptions = project.getGeneratorOptions();
        try {
            CodePluginItem itemInput = findItemByUuidOrDefault(inputsSupported, genOptions.getLastInputUuid());
            inputCreator = manager.makeCreator(itemInput);
            currentInput = new UICodePluginItem(manager, itemInput, CHANGE, this::onInputChange);
            currentInput.getStyleClass().add("uiCodeGen");
            vbox.getChildren().add(currentInput);
        } catch (ClassNotFoundException e) {
            logger.log(ERROR, "Class loading problem", e);
        }


        addTitleLabel(vbox, "Select the display type:");
        try {
            CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, genOptions.getLastDisplayUuid());
            currentDisplay = new UICodePluginItem(manager, itemDisplay, CHANGE, this::onDisplayChange);
            displayCreator = manager.makeCreator(itemDisplay);
            currentDisplay.getStyleClass().add("uiCodeGen");
            vbox.getChildren().add(currentDisplay);
        } catch (ClassNotFoundException e) {
            logger.log(ERROR, "Class loading problem", e);
        }

        addTitleLabel(vbox, "Select remote capabilities:");
        try {
            CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, genOptions.getLastRemoteCapabilitiesUuid());
            currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange);
            remoteCreator = manager.makeCreator(itemRemote);
            currentRemote.getStyleClass().add("uiCodeGen");
            vbox.getChildren().add(currentRemote);
        } catch (ClassNotFoundException e) {
            logger.log(ERROR, "Class loading problem", e);
        }

        buildTable();

        ButtonBar buttonBar = new ButtonBar();
        generateButton = new Button("Generate Code");
        generateButton.setDefaultButton(true);
        generateButton.setOnAction(this::onGenerateCode);
        cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(this::onCancel);
        buttonBar.getButtons().addAll(generateButton, cancelButton);

        BorderPane root = new BorderPane();
        root.setTop(vbox);
        root.setOpaqueInsets(new Insets(5));
        root.setCenter(propsTable);
        root.setBottom(buttonBar);
        root.setPrefSize(800, 750);
        BorderPane.setMargin(propsTable, new Insets(5));
        BorderPane.setMargin(buttonBar, new Insets(5));
        BorderPane.setMargin(vbox, new Insets(5));

        dialogStage = new Stage();
        dialogStage.setTitle("Code Generator:" + project.getFileName());
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(stage);
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/ui/JMetroDarkTheme.css").toExternalForm());
        dialogStage.setScene(scene);
        if(modal) {
            dialogStage.showAndWait();
        }
        else {
            dialogStage.show();
        }

    }

    private void buildTable() {
        propsTable = new TableView<>();
        defineCol = new TableColumn<>("Parameter");
        typeCol = new TableColumn<>("SubSystem");
        valueCol = new TableColumn<>("Value");
        descriptionCol = new TableColumn<>("Description");
        descriptionCol.setPrefWidth(400);
        propsTable.getColumns().addAll(defineCol, typeCol, valueCol, descriptionCol);
        propsTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        propsTable.setMaxHeight(2000);

        defineCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getName()));
        typeCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getSubsystem().toString()));
        valueCol.setCellValueFactory(param -> param.getValue().getProperty());
        valueCol.setPrefWidth(130);
        descriptionCol.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().getDescription()));

        propsTable.setEditable(true);
        valueCol.setEditable(true);

        valueCol.setCellFactory(editCol -> new CreatorEditingTableCell(editorUI));

        changeProperties();
    }

    private void addTitleLabel(VBox vbox, String text) {
        Label titleLbl = new Label(text);
        titleLbl.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        vbox.getChildren().add(titleLbl);
    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid) {
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElse(items.get(0));
    }

    private void placeDirectoryAndEmbeddedPanels(VBox vbox) {
        BorderPane embeddedPane = new BorderPane();
        embeddedPane.setLeft(new Label("Embedded Platform"));
        platformCombo = new ComboBox<>(observableArrayList(EmbeddedPlatform.values()));
        embeddedPane.setRight(platformCombo);
        vbox.getChildren().add(embeddedPane);
        platformCombo.getSelectionModel().select(project.getGeneratorOptions().getEmbeddedPlatform());
        platformCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> filterChoicesByPlatform(newVal));
    }

    private void filterChoicesByPlatform(EmbeddedPlatform newVal) {
        displaysSupported = manager.getPluginsThatMatch(newVal, DISPLAY);
        inputsSupported = manager.getPluginsThatMatch(newVal, INPUT);
        remotesSupported = manager.getPluginsThatMatch(newVal, REMOTE);
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

        propsTable.setItems(observableArrayList(properties));
    }

    private void setAllPropertiesToLastValues(List<CreatorProperty> propsToDefault) {
        propsToDefault.forEach(prop -> project.getGeneratorOptions().getLastProperties().stream()
                .filter(p-> prop.getName().equals(p.getName()) && prop.getSubsystem().equals(p.getSubsystem()))
                .findFirst()
                .ifPresent(p-> prop.getProperty().set(p.getLatestValue())));
    }


    private void onCancel(ActionEvent actionEvent) {
        dialogStage.close();
    }

    private void onGenerateCode(ActionEvent actionEvent) {
        var allProps = new ArrayList<CreatorProperty>();
        allProps.addAll(displayCreator.properties());
        allProps.addAll(inputCreator.properties());
        allProps.addAll(remoteCreator.properties());

        project.setGeneratorOptions(new CodeGeneratorOptions(
                platformCombo.getSelectionModel().getSelectedItem(),
                currentDisplay.getItem().getId(), currentInput.getItem().getId(), currentRemote.getItem().getId(), allProps));

        runner.startCodeGeneration(mainStage, platformCombo.getSelectionModel().getSelectedItem(),
                                   Paths.get(project.getFileName()).getParent().toString(),
                                   Arrays.asList(displayCreator, inputCreator, remoteCreator));

        dialogStage.close();
    }

    private void onDisplayChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on display");
        selectPlugin(displaysSupported, "Display", (pluginItem)-> {
            try {
                displayCreator = manager.makeCreator(pluginItem);
            } catch (ClassNotFoundException e) {
                logger.log(ERROR, "Unable to create the display creator" + item);
            }
            currentDisplay.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onRemoteChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on remote");
        selectPlugin(remotesSupported, "Remote", (pluginItem)-> {
            try {
                remoteCreator = manager.makeCreator(pluginItem);
            } catch (ClassNotFoundException e) {
                logger.log(ERROR, "Unable to create the remote creator" + item);
            }
            currentRemote.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onInputChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on input");
        selectPlugin(inputsSupported, "Input", (pluginItem)-> {
            try {
                inputCreator = manager.makeCreator(pluginItem);
            } catch (ClassNotFoundException e) {
                logger.log(ERROR, "Unable to create the input creator" + item);
            }
            currentInput.setItem(pluginItem);
            changeProperties();
        });
    }


    private void selectPlugin(List<CodePluginItem> pluginItems, String changeWhat, Consumer<CodePluginItem> eventHandler) {

        Popup popup = new Popup();
        List<UICodePluginItem> listOfComponents = pluginItems.stream()
                .map(display -> new UICodePluginItem(manager, display, SELECT, item -> {
                    popup.hide();
                    eventHandler.accept(item);
                }))
                .collect(Collectors.toList());

        VBox vbox = new VBox(5);
        addTitleLabel(vbox, "Select the " + changeWhat + " to use:");
        vbox.getChildren().addAll(listOfComponents);
        vbox.setPrefSize(700, 600);
        vbox.setStyle("-fx-background-color: #f4f0ff; -fx-border-style: solid;-fx-border-color: black; -fx-border-width: 1px; -fx-border-insets: 2px;-fx-background-insets: 2px;");

        popup.getContent().add(vbox);
        popup.setAutoHide(true);
        popup.hideOnEscapeProperty();
        popup.show(dialogStage);
    }

}
