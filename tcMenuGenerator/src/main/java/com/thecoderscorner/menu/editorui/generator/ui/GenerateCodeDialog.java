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
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.nio.file.Paths;
import java.util.*;
import java.util.function.BiConsumer;
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
    private final static String DEFAULT_THEME_ID = "b186c809-d9ef-4ca8-9d4b-e4780a041ccc"; // manual theme, works for most displays

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
    private List<UICodePluginItem> currentRemotes = new ArrayList<>();

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
    private VBox centerPane;

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

        centerPane = new VBox(5);
        addTitleLabel(centerPane, "Select the input type:");
        CodeGeneratorOptions genOptions = project.getGeneratorOptions();
        var allItems = project.getMenuTree().getAllMenuItems();

        CodePluginItem itemInput = findItemByUuidOrDefault(inputsSupported, genOptions.getLastInputUuid(), Optional.empty());
        CodePluginItem itemDisplay = findItemByUuidOrDefault(displaysSupported, genOptions.getLastDisplayUuid(), Optional.empty());
        CodePluginItem itemTheme = findItemByUuidOrDefault(themesSupported, genOptions.getLastThemeUuid(), Optional.of(DEFAULT_THEME_ID));

        addToInitialSourceFilesForRemovalCheck(itemInput);
        addToInitialSourceFilesForRemovalCheck(itemTheme);
        addToInitialSourceFilesForRemovalCheck(itemDisplay);

        setAllPropertiesToLastValues(itemInput);
        setAllPropertiesToLastValues(itemDisplay);
        if(itemTheme != null) {
            setAllPropertiesToLastValues(itemTheme);
        }

        currentInput = new UICodePluginItem(manager, itemInput, CHANGE, this::onInputChange, editorUI, allItems, 0);
        currentInput.setId("currentInputUI");
        currentInput.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentInput);

        addTitleLabel(centerPane, "Select the display type:");
        currentDisplay = new UICodePluginItem(manager, itemDisplay, CHANGE, this::onDisplayChange, editorUI, allItems, 0);
        currentDisplay.setId("currentDisplayUI");
        currentDisplay.getStyleClass().add("uiCodeGen");
        centerPane.getChildren().add(currentDisplay);

        if(itemTheme != null) {
            themeTitle = addTitleLabel(centerPane, "Select a theme:");
            currentTheme = new UICodePluginItem(manager, itemTheme, CHANGE, this::onThemeChange, editorUI, allItems, 0);
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

        BorderPane remoteLabelPane = new BorderPane();
        Label titleLbl = new Label("Select IoT/remote capabilities:");
        titleLbl.getStyleClass().add("label-bright");
        remoteLabelPane.setLeft(titleLbl);

        Button addRemoteCapabilityButton = new Button("Add another IoT/remote");
        remoteLabelPane.setRight(addRemoteCapabilityButton);
        addRemoteCapabilityButton.setOnAction(this::produceAnotherRemoteCapability);
        centerPane.getChildren().add(remoteLabelPane);

        List<String> remoteIds = genOptions.getLastRemoteCapabilitiesUuids();
        if(remoteIds != null && !remoteIds.isEmpty()) {
            int count = 0;
            for(var remoteId : remoteIds) {
                CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, remoteId, Optional.of(CoreCodeGenerator.NO_REMOTE_ID));
                addToInitialSourceFilesForRemovalCheck(itemRemote);
                setAllPropertiesToLastValues(itemRemote);
                var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI, allItems, count);
                currentRemote.setId("currentRemoteUI-" + count);
                currentRemote.getStyleClass().add("uiCodeGen");
                centerPane.getChildren().add(currentRemote);
                count++;
                currentRemotes.add(currentRemote);
            }
        }
        else {
            // did not exist before this, must be first run.
            CodePluginItem itemRemote = findItemByUuidOrDefault(remotesSupported, "", Optional.empty());
            var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI, allItems, 0);
            currentRemote.setId("currentRemoteUI-0");
            currentRemote.getStyleClass().add("uiCodeGen");
            centerPane.getChildren().add(currentRemote);
        }

        ButtonBar buttonBar = new ButtonBar();
        generateButton = new Button("Generate Code");
        generateButton.setDefaultButton(true);
        generateButton.setOnAction(this::onGenerateCode);
        generateButton.setId("generateButton");
        cancelButton = new Button("Cancel");
        cancelButton.setCancelButton(true);
        cancelButton.setOnAction(this::onCancel);
        buttonBar.getButtons().addAll(cancelButton, generateButton);

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

    private void produceAnotherRemoteCapability(ActionEvent actionEvent) {
        var itemRemote = findItemByUuidOrDefault(remotesSupported, CoreCodeGenerator.NO_REMOTE_ID, Optional.empty());
        setAllPropertiesToLastValues(itemRemote);
        var allItems = project.getMenuTree().getAllMenuItems();
        var currentRemote = new UICodePluginItem(manager, itemRemote, CHANGE, this::onRemoteChange, editorUI,
                allItems, currentRemotes.size());
        currentRemote.setId("currentRemoteUI-" + currentRemotes.size());
        currentRemote.getStyleClass().add("uiCodeGen");
        currentRemotes.add(currentRemote);
        centerPane.getChildren().add(currentRemote);
    }

    private void addToInitialSourceFilesForRemovalCheck(CodePluginItem pluginItem) {
        if(pluginItem == null) return;
        for(var sf : pluginItem.getRequiredSourceFiles()) {
            if(sf.isOverwritable()) {
                initialPlugins.add(sf.getFileName());
            }
        }
    }

    private Label addTitleLabel(Pane vbox, String text) {
        Label titleLbl = new Label(text);
        titleLbl.getStyleClass().add("label-bright");
        vbox.getChildren().add(titleLbl);
        return titleLbl;
    }

    private CodePluginItem findItemByUuidOrDefault(List<CodePluginItem> items, String uuid, Optional<String> maybeDefault) {
        if(items.size() == 0) throw new IllegalStateException("No plugins have been loaded");
        return items.stream().filter(item -> item.getId().equals(uuid)).findFirst().orElseGet(() -> {
            CodePluginItem ret;
            if(maybeDefault.isPresent()) {
                ret = items.stream().filter(item -> item.getId().equals(maybeDefault.get())).findFirst().orElse(items.get(0));
            }
            else ret = items.get(0);
            return ret;
        });
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
        platformCombo.setId("platformCombo");
        platformCombo.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldVal, newVal) -> filterChoicesByPlatform(newVal)
        );

        var uuid = project.getGeneratorOptions().getApplicationUUID();
        if(uuid == null) uuid = UUID.randomUUID();
        appUuidField = new TextField(uuid.toString());
        appUuidField.setDisable(true);
        appUuidField.setId("appUuidField");
        embeddedPane.add(appUuidField, 1, 1);
        Button newAppUuidButton = new Button("Change");
        newAppUuidButton.setTooltip(new Tooltip("Application UUID's identify your app to remote API/UI's, avoid changing"));
        newAppUuidButton.setOnAction(this::onNewUUIDRequired);
        newAppUuidButton.setId("appUuidButton");
        embeddedPane.add(newAppUuidButton, 2, 1);

        var appName = project.getGeneratorOptions().getApplicationName();
        if(appName == null || appName.isEmpty()) {
            appName = "New app";
        }
        appNameField = new TextField(appName);
        appNameField.setId("appNameField");
        appNameField.setTooltip(new Tooltip("Application names appear on the display and also on remote connections"));
        embeddedPane.add(appNameField, 1, 2);

        recursiveNamingCheckBox = new CheckBox("Use menu names that are fully qualified (EG: menuSubNameChildName)");
        recursiveNamingCheckBox.setSelected(project.getGeneratorOptions().isNamingRecursive());
        recursiveNamingCheckBox.setId("recursiveNaming");
        embeddedPane.add(recursiveNamingCheckBox, 1, 3, 2, 1);

        saveToSrcCheckBox = new CheckBox("Save all CPP and H files into src folder");
        saveToSrcCheckBox.setSelected(project.getGeneratorOptions().isSaveToSrc());
        saveToSrcCheckBox.setId("saveToSrc");
        embeddedPane.add(saveToSrcCheckBox, 1, 4, 2, 1);

        useCppMainCheckBox = new CheckBox("Use a CPP file for main (Arduino only)");
        useCppMainCheckBox.setSelected(project.getGeneratorOptions().isUseCppMain());
        useCppMainCheckBox.setId("useCppMain");
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
                "The application will be treated as new by all IoT/remote and API apps.")) {
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

    private void setAllPropertiesToLastValues(CodePluginItem itemToSetFor) {
        if(itemToSetFor == null || itemToSetFor.getProperties() == null) return;

        for(var prop :  itemToSetFor.getProperties()) {
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
        for(var remote : currentRemotes) {
            allProps.addAll(remote.getItem().getProperties());
        }
        if(currentTheme != null) {
            allProps.addAll(currentTheme.getItem().getProperties());
        }

        UUID applicationUUID = UUID.fromString(appUuidField.getText());
        String themeId = currentTheme != null ? currentTheme.getItem().getId() : "";
        project.setGeneratorOptions(new CodeGeneratorOptions(
                platformCombo.getSelectionModel().getSelectedItem().getBoardId(),
                currentDisplay.getItem().getId(), currentInput.getItem().getId(),
                currentRemotes.stream().map(r-> r.getItem().getId()).collect(Collectors.toList()), themeId,
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
        var allPlugins = new ArrayList<CodePluginItem>();
        allPlugins.add(currentDisplay.getItem());
        allPlugins.add(currentInput.getItem());
        for(var pl : currentRemotes) {
            allPlugins.add(pl.getItem());
        }
        if(currentDisplay.getItem().isThemeNeeded() && currentTheme != null) {
            allPlugins.add(currentTheme.getItem());
        }
        return allPlugins;
    }

    private void onDisplayChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on display");
        selectPlugin(displaysSupported, "Display", (uiPluginLocal, pluginItem)-> {
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
        List<CodePluginItem> creators = getAllPluginsForConversion();

        creators.stream()
                .filter(p -> p != null && p.getProperties().size() > 0)
                .forEach(this::setAllPropertiesToLastValues);
    }

    private void onRemoteChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        if(item == null) {
            logger.log(INFO, "Remove fired on remote");
            currentRemotes.remove(uiPlugin);
            centerPane.getChildren().remove(uiPlugin);
            changeProperties();

            for(int i=0; i<currentRemotes.size(); i++) {
                currentRemotes.get(i).setItemIndex(i);
            }

            return;
        }
        logger.log(INFO, "Action fired on remote");
        selectPlugin(remotesSupported, "Remote", (uiPluginLocal, pluginItem)-> {
            if (uiPluginLocal.getItemIndex() < currentRemotes.size()) {
                currentRemotes.get(uiPlugin.getItemIndex()).setItem(pluginItem);
                changeProperties();
            }
        });
    }

    private void onThemeChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on theme");
        selectPlugin(themesSupported, "Theme", (uiLocal, pluginItem)-> {
            currentTheme.setItem(pluginItem);
            changeProperties();
        });
    }

    private void onInputChange(UICodePluginItem uiPlugin, CodePluginItem item) {
        logger.log(INFO, "Action fired on input");
        selectPlugin(inputsSupported, "Input", (uiLocal, pluginItem)-> {
            currentInput.setItem(pluginItem);
            changeProperties();
        });
    }

    private void selectPlugin(List<CodePluginItem> pluginItems, String changeWhat, BiConsumer<UICodePluginItem, CodePluginItem> eventHandler) {

        Popup popup = new Popup();
        List<UICodePluginItem> listOfComponents = pluginItems.stream()
                .map(display -> {
                    var it = new UICodePluginItem(manager, display, SELECT, (ui, item) -> {
                        popup.hide();
                        eventHandler.accept(ui, item);
                    }, 0);
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
