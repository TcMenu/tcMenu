/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */
/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatforms;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.UiHelper;
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

import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.*;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.CHANGE;
import static com.thecoderscorner.menu.editorui.generator.ui.UICodePluginItem.UICodeAction.SELECT;
import static com.thecoderscorner.menu.editorui.util.UiHelper.createDialogStateAndShowSceneAdj;
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
    private List<CodePluginItem> themesSupported;
    private List<String> initialPlugins = new ArrayList<>();

    private UICodePluginItem currentDisplay;
    private UICodePluginItem currentTheme;
    private UICodePluginItem currentInput;
    private UICodePluginItem currentRemote;

    private ComboBox<EmbeddedPlatform> platformCombo;
    private Button generateButton;
    private Button cancelButton;
    private TextField appUuidField;
    private TextField appNameField;
    private CheckBox recursiveNamingCheckBox;
    private CheckBox saveToSrcCheckBox;
    private CheckBox useCppMainCheckBox;
    private Stage mainStage;

    private List<CreatorProperty> properties = new ArrayList<>();
    private Stage dialogStage;
    private Label themeTitle;

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
        BorderPane pane = new BorderPane();

        placeDirectoryAndEmbeddedPanels(pane);
        filterChoicesByPlatform(platformCombo.getValue());

        VBox centerPane = new VBox(5);
        addTitleLabel(centerPane, "Select the input type:");
        CodeGeneratorOptions genOptions = project.getGeneratorOptions();
        var allItems = project.getMenuTree().getAllMenuItems();

        CodePluginItem itemInput = findItemByUuidOrDefault(inputsSupported, genOptions.getLastInputUuid());
        CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, genOptions.getLastDisplayUuid());
        CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, genOptions.getLastRemoteCapabilitiesUuid());
        CodePluginItem itemTheme = findItemByUuidOrDefault(themesSupported, genOptions.getLastThemeUuid());

        addToInitialSourceFilesForRemovalCheck(itemInput);
        addToInitialSourceFilesForRemovalCheck(itemTheme);
        addToInitialSourceFilesForRemovalCheck(itemDisplay);
        addToInitialSourceFilesForRemovalCheck(itemRemote);

        setAllPropertiesToLastValues(itemInput.getProperties());
        setAllPropertiesToLastValues(itemDisplay.getProperties());
        setAllPropertiesToLastValues(itemRemote.getProperties());
        if(itemTheme != null) {
            setAllPropertiesToLastValues(itemTheme.getProperties());
        }

        currentInput = new UICodePluginItem(manager, itemInput, CHANGE, this::onInputChange, editorUI, allItems);
        currentInput.setId("currentInputUI");
        currentInput.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentInput);

        addTitleLabel(centerPane, "Select the display type:");
        currentDisplay = new UICodePluginItem(manager, itemDisplay, CHANGE, this::onDisplayChange, editorUI, allItems);
        currentDisplay.setId("currentDisplayUI");
        currentDisplay.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentDisplay);

        if(itemTheme != null) {
            themeTitle = addTitleLabel(centerPane, "Select a theme:");
            currentTheme = new UICodePluginItem(manager, itemTheme, CHANGE, this::onThemeChange, editorUI, allItems);
            currentTheme.setId("currentThemeUI");
            currentTheme.getStyleClass().add("uiCodeGen");
            if (!currentDisplay.getItem().isThemeNeeded()) {
                currentTheme.setVisible(false);
                currentTheme.setManaged(false);
                themeTitle.setVisible(false);
                themeTitle.setManaged(false);
            }
            centerPane.getChildren().add(currentTheme);
        }
        else currentTheme = null;

        addTitleLabel(centerPane, "Select remote capabilities:");
        currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI, allItems);
        currentRemote.setId("currentRemoteUI");
        currentRemote.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentRemote);

        ButtonBar buttonBar = new ButtonBar();
        generateButton = new Button("Generate Code");
        generateButton.setDefaultButton(true);
        generateButton.setOnAction(this::onGenerateCode);
        generateButton.setId("GenerateButton");
        cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(this::onCancel);
        buttonBar.getButtons().addAll(generateButton, cancelButton);

        ScrollPane scrollPane = new ScrollPane(centerPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        pane.setCenter(scrollPane);
        pane.setOpaqueInsets(new Insets(5));
        pane.setBottom(buttonBar);
        pane.setPrefSize(800, 750);
        BorderPane.setMargin(buttonBar, new Insets(5));
        BorderPane.setMargin(pane.getTop(), new Insets(5));


        var title = "Code Generator:" + project.getFileName();
        createDialogStateAndShowSceneAdj(stage, pane, title, modal, (scene, dlgStg) -> {
            scene.getStylesheets().add(UiHelper.class.getResource("/ui/tcmenu-extras.css").toExternalForm());
            dialogStage = dlgStg;
        });
    }

    private void addToInitialSourceFilesForRemovalCheck(CodePluginItem pluginItem) {
        if(pluginItem == null) return;
        for(var sf : pluginItem.getRequiredSourceFiles()) {
            if(sf.isOverwritable()) {
                initialPlugins.add(sf.getFileName());
            }
        }
    }

    private Label addTitleLabel(VBox vbox, String text) {
        Label titleLbl = new Label(text);
        titleLbl.getStyleClass().add("label-bright");
        vbox.getChildren().add(titleLbl);
        return titleLbl;
    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid) {
        if(items.size() == 0) return null;
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElse(items.get(0));
    }

    private void placeDirectoryAndEmbeddedPanels(BorderPane pane) {
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

        saveToSrcCheckBox = new CheckBox("Save all CPP and H files into src folder");
        saveToSrcCheckBox.setSelected(project.getGeneratorOptions().isSaveToSrc());
        embeddedPane.add(saveToSrcCheckBox, 1, 4, 2, 1);

        useCppMainCheckBox = new CheckBox("Use a CPP file for main (Arduino only)");
        useCppMainCheckBox.setSelected(project.getGeneratorOptions().isUseCppMain());
        embeddedPane.add(useCppMainCheckBox, 1, 5, 2, 1);

        ColumnConstraints column1 = new ColumnConstraints(120);
        ColumnConstraints column2 = new ColumnConstraints(350);
        ColumnConstraints column3 = new ColumnConstraints(80);
        embeddedPane.getColumnConstraints().add(column1);
        embeddedPane.getColumnConstraints().add(column2);
        embeddedPane.getColumnConstraints().add(column3);
        pane.setTop(embeddedPane);

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
        themesSupported = manager.getPluginsThatMatch(newVal, THEME);
        useCppMainCheckBox.setDisable(platformCombo.getValue().equals(EmbeddedPlatform.MBED_RTOS));
    }

    private void setAllPropertiesToLastValues(List<CreatorProperty> propsToDefault) {
        for(var prop :  propsToDefault) {
            var lastProp = project.getGeneratorOptions().getLastProperties().stream()
                    .filter(p -> prop.getName().equals(p.getName()) && prop.getSubsystem().equals(p.getSubsystem()))
                    .findFirst();
            if (lastProp.isPresent()) {
                prop.setLatestValue(lastProp.get().getLatestValue());
            } else {
                prop.resetToInitial();
            }
        }
    }


    private void onCancel(ActionEvent actionEvent) {
        dialogStage.close();
    }

    private void onGenerateCode(ActionEvent actionEvent) {
        var allProps = new ArrayList<CreatorProperty>();
        allProps.addAll(currentDisplay.getItem().getProperties());
        allProps.addAll(currentInput.getItem().getProperties());
        allProps.addAll(currentRemote.getItem().getProperties());
        if(currentTheme != null) {
            allProps.addAll(currentTheme.getItem().getProperties());
        }

        UUID applicationUUID = UUID.fromString(appUuidField.getText());
        String themeId = currentTheme != null ? currentTheme.getItem().getId() : "";
        project.setGeneratorOptions(new CodeGeneratorOptions(
                platformCombo.getSelectionModel().getSelectedItem().getBoardId(),
                currentDisplay.getItem().getId(), currentInput.getItem().getId(), currentRemote.getItem().getId(), themeId,
                allProps, applicationUUID, appNameField.getText(), recursiveNamingCheckBox.isSelected(),
                saveToSrcCheckBox.isSelected(), useCppMainCheckBox.isSelected())
        );

        runner.startCodeGeneration(mainStage, platformCombo.getSelectionModel().getSelectedItem(),
                                   Paths.get(project.getFileName()).getParent().toString(),
                                   getAllPluginsForConversion(),
                                   initialPlugins,
                                   true);

        dialogStage.close();
    }

    private List<CodePluginItem> getAllPluginsForConversion() {
        if(currentDisplay.getItem().isThemeNeeded() && currentTheme != null) {
            return Arrays.asList(currentDisplay.getItem(), currentInput.getItem(), currentRemote.getItem(), currentTheme.getItem());
        }
        else {
            return Arrays.asList(currentDisplay.getItem(), currentInput.getItem(), currentRemote.getItem());
        }
    }

    private void onDisplayChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on display");
        selectPlugin(displaysSupported, "Display", (pluginItem)-> {
            currentDisplay.setItem(pluginItem);
            changeProperties();
            if(currentTheme == null) return;
            boolean themeNeeded = currentDisplay.getItem().isThemeNeeded();
            currentTheme.setVisible(themeNeeded);
            currentTheme.setManaged(themeNeeded);
            themeTitle.setVisible(themeNeeded);
            themeTitle.setManaged(themeNeeded);
        });
    }

    private void changeProperties() {
        List<CodePluginItem> creators;
        if(currentDisplay.getItem().isThemeNeeded() && currentTheme != null) {
            creators = Arrays.asList(currentDisplay.getItem(), currentInput.getItem(), currentRemote.getItem(), currentTheme.getItem());
        }
        else {
            creators = Arrays.asList(currentDisplay.getItem(), currentInput.getItem(), currentRemote.getItem());
        }

        creators.stream()
                .filter(p -> p != null && p.getProperties().size() > 0)
                .forEach( creator -> {
                    setAllPropertiesToLastValues(creator.getProperties());
                });
    }

    private void onRemoteChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on remote");
        selectPlugin(remotesSupported, "Remote", (pluginItem)-> {
            currentRemote.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onThemeChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on theme");
        selectPlugin(themesSupported, "Theme", (pluginItem)-> {
            currentTheme.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onInputChange(CodePluginItem item) {
        logger.log(INFO, "Action fired on input");
        selectPlugin(inputsSupported, "Input", (pluginItem)-> {
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

        BorderPane pane = new BorderPane();
        pane.setCenter(vbox);
        vbox.getStyleClass().add("popupWindow");

        var scroll = new ScrollPane(pane);
        scroll.setFitToWidth(true);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setPrefSize(700, 600);
        popup.getContent().add(scroll);
        popup.setAutoHide(true);
        popup.setOnAutoHide(event -> popup.hide());
        popup.setHideOnEscape(true);
        popup.show(dialogStage);
    }
}
