/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.UiHelper;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.CHANGE;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.SELECT;
import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShowSceneAdj;
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
    private final EmbeddedPlatforms platforms;

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
    private TextField appUuidField;
    private TextField appNameField;
    private CheckBox recursiveNamingCheckBox;
    private Stage mainStage;

    public TableView<CreatorProperty> propsTable;
    public TableColumn<CreatorProperty, String> defineCol;
    public TableColumn<CreatorProperty, String> typeCol;
    public TableColumn<CreatorProperty, String> valueCol;
    public TableColumn<CreatorProperty, String> descriptionCol;
    private List<CreatorProperty> properties = new ArrayList<>();
    private Stage dialogStage;


    public GenerateCodeDialog(CodePluginManager manager, CurrentProjectEditorUI editorUI,
                              CurrentEditorProject project, CodeGeneratorRunner runner,
                              EmbeddedPlatforms platforms) {
        this.manager = manager;
        this.editorUI = editorUI;
        this.project = project;
        this.runner = runner;
        this.platforms = platforms;
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
            currentInput.setId("currentInputUI");
            currentInput.getStyleClass().add("uiCodeGen");
            vbox.getChildren().add(currentInput);
        } catch (ClassNotFoundException e) {
            logger.log(ERROR, "Class loading problem", e);

        }

        addTitleLabel(vbox, "Select the display type:");
        try {
            CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, genOptions.getLastDisplayUuid());
            currentDisplay = new UICodePluginItem(manager, itemDisplay, CHANGE, this::onDisplayChange);
            currentDisplay.setId("currentDisplayUI");
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
            currentRemote.setId("currentRemoteUI");
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
        generateButton.setId("GenerateButton");
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

        var title = "Code Generator:" + project.getFileName();
        createDialogStateAndShowSceneAdj(stage, root, title, modal, (scene, dlgStg) -> {
            scene.getStylesheets().add(UiHelper.class.getResource("/ui/JMetroDarkTheme.css").toExternalForm());
            dialogStage = dlgStg;
        });
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
        titleLbl.getStyleClass().add("label-bright");
        vbox.getChildren().add(titleLbl);
    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid) {
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElse(items.get(0));
    }

    private void placeDirectoryAndEmbeddedPanels(VBox vbox) {
        GridPane embeddedPane = new GridPane();
        embeddedPane.setHgap(5);
        embeddedPane.setVgap(3);
        embeddedPane.add(new Label("Embedded Platform"), 0, 0);
        embeddedPane.add(new Label("Application UUID"), 0, 1);
        embeddedPane.add(new Label("Application Name"), 0, 2);

        platformCombo = new ComboBox<>(observableArrayList(platforms.getEmbeddedPlatforms()));
        embeddedPane.add(platformCombo, 1, 0, 2, 1);
        EmbeddedPlatform platform = getLastEmbeddedPlatform();
        platformCombo.getSelectionModel().select(platform);
        platformCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> filterChoicesByPlatform(newVal)
        );

        var uuid = project.getGeneratorOptions().getApplicationUUID();
        if(uuid == null) uuid = UUID.randomUUID();
        appUuidField = new TextField(uuid.toString());
        appUuidField.setDisable(true);
        appUuidField.setId("appuuid");
        embeddedPane.add(appUuidField, 1, 1);
        Button newAppUuidButton = new Button("Change");
        newAppUuidButton.setTooltip(new Tooltip("Application UUID's identify your app to remote API/UI's, avoid changing"));
        newAppUuidButton.setOnAction(this::onNewUUIDRequired);
        newAppUuidButton.setId("newuuidbtn");
        embeddedPane.add(newAppUuidButton, 2, 1);

        var appName = project.getGeneratorOptions().getApplicationName();
        if(appName == null || appName.isEmpty()) {
            appName = "New app";
        }
        appNameField = new TextField(appName);
        appNameField.setId("appname");
        appNameField.setTooltip(new Tooltip("Application names appear on the display and also on remote connections"));
        embeddedPane.add(appNameField, 1, 2);

        recursiveNamingCheckBox = new CheckBox("Use menu names that are fully qualified (EG: menuSubNameChildName)");
        recursiveNamingCheckBox.setSelected(project.getGeneratorOptions().isNamingRecursive());
        embeddedPane.add(recursiveNamingCheckBox, 1, 3, 2, 1);

        ColumnConstraints column1 = new ColumnConstraints(120);
        ColumnConstraints column2 = new ColumnConstraints(350);
        ColumnConstraints column3 = new ColumnConstraints(80);
        embeddedPane.getColumnConstraints().add(column1);
        embeddedPane.getColumnConstraints().add(column2);
        embeddedPane.getColumnConstraints().add(column3);
        vbox.getChildren().add(embeddedPane);

    }

    private EmbeddedPlatform getLastEmbeddedPlatform() {
        var platform = EmbeddedPlatform.ARDUINO_AVR;
        String lastPlatform = project.getGeneratorOptions().getEmbeddedPlatform();
        try {
            platform = platforms.getEmbeddedPlatformFromId(lastPlatform);
        }
        catch (Exception e) {
            logger.log(ERROR, "Chosen platform could not be loaded back." + lastPlatform, e);
            editorUI.alertOnError(
                    "Platform changed",
                    "The platform " + lastPlatform + "is no longer available, defaulting to AVR"
            );
        }
        return platform;
    }

    private void onNewUUIDRequired(ActionEvent actionEvent) {
        if(editorUI.questionYesNo(
                "Really change the UUID?",
                "The application will be treated as new by all remote and API apps.")) {
            appUuidField.setText(UUID.randomUUID().toString());
        }
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

        UUID applicationUUID = UUID.fromString(appUuidField.getText());
        project.setGeneratorOptions(new CodeGeneratorOptions(
                platformCombo.getSelectionModel().getSelectedItem().getBoardId(),
                currentDisplay.getItem().getId(), currentInput.getItem().getId(), currentRemote.getItem().getId(),
                allProps, applicationUUID, appNameField.getText(), recursiveNamingCheckBox.isSelected())
        );

        runner.startCodeGeneration(mainStage, platformCombo.getSelectionModel().getSelectedItem(),
                                   Paths.get(project.getFileName()).getParent().toString(),
                                   Arrays.asList(displayCreator, inputCreator, remoteCreator), true);

        dialogStage.close();
    }

    private void onDisplayChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on display");
        selectPlugin(displaysSupported, "Display", (pluginItem)-> {
            try {
                displayCreator = manager.makeCreator(pluginItem);
            } catch (ClassNotFoundException e) {
                logger.log(ERROR, "Unable to create the display creator" + item);
                editorUI.alertOnError(
                        "Fault loading display plugin",
                        "Unable to load " + pluginItem.getDescription() + " - " + pluginItem.getCodeCreatorClass());
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
                editorUI.alertOnError(
                        "Fault loading remote plugin",
                        "Unable to load " + pluginItem.getDescription() + " - " + pluginItem.getCodeCreatorClass());
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
                editorUI.alertOnError(
                        "Fault loading input plugin",
                        "Unable to load " + pluginItem.getDescription() + " - " + pluginItem.getCodeCreatorClass());

            }
            currentInput.setItem(pluginItem);
            changeProperties();
        });
    }


    private void selectPlugin(List<CodePluginItem> pluginItems, String changeWhat, Consumer<CodePluginItem> eventHandler) {

        Popup popup = new Popup();
        List<UICodePluginItem> listOfComponents = pluginItems.stream()
                .map(display -> {
                    var it = new UICodePluginItem(manager, display, SELECT, item -> {
                        popup.hide();
                        eventHandler.accept(item);
                    });
                    it.setId("sel-" + display.getId());

                    return it;
                })
                .collect(Collectors.toList());

        VBox vbox = new VBox(5);
        addTitleLabel(vbox, "Select the " + changeWhat + " to use:");
        vbox.getChildren().addAll(listOfComponents);
        vbox.setPrefSize(700, 600);

        BorderPane pane = new BorderPane();
        pane.setCenter(vbox);
        vbox.getStyleClass().add("popupWindow");

        popup.getContent().add(pane);
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> popup.hide());
        popup.setHideOnEscape(true);
        popup.show(dialogStage);
    }
}
