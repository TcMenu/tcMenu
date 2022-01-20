/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.dialog.ChooseFontDialog;
import com.thecoderscorner.menu.editorui.dialog.ChooseIoExpanderDialog;
import com.thecoderscorner.menu.editorui.generator.applicability.CodeApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinitionCollection;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.validation.*;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class UICodePluginItem extends BorderPane {

    public static final int IMG_THUMB_WIDTH = 150;

    public enum UICodeAction { CHANGE, SELECT }

    private int itemIndex;
    private final boolean showProperties;
    private final BiConsumer<UICodePluginItem, CodePluginItem> eventHandler;
    private final Label titleLabel;
    private final Collection<MenuItem> allItems;
    private final Label descriptionArea;
    private final Label whichPlugin;
    private final Hyperlink licenseLink;
    private final Hyperlink vendorLink;
    private final Hyperlink docsLink;
    private final CodePluginManager mgr;
    private final Button actionButton;
    private final VBox propertiesPanel;
    private final CurrentProjectEditorUI editorUI;
    private CodePluginItem item;
    private final Map<String, PropertyWithControl> propertiesById = new HashMap<>();

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action,
                            BiConsumer<UICodePluginItem, CodePluginItem> evt, int itemIndex, String id) {
        this(mgr, item, action, evt, null, null, itemIndex, id);
    }

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action,
                            BiConsumer<UICodePluginItem, CodePluginItem> evt,
                            CurrentProjectEditorUI editorUI, Collection<com.thecoderscorner.menu.domain.MenuItem> allItems,
                            int itemIndex, String id) {
        super();

        setId(id);
        showProperties = allItems != null && editorUI != null;

        this.itemIndex = itemIndex;
        this.editorUI = editorUI;
        this.eventHandler = evt;

        this.mgr = mgr;
        this.item = item;

        titleLabel = new Label(item.getDescription());
        this.allItems = allItems;
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");
        titleLabel.setId(makeAnId("Title"));

        Node titleNode;
        if(itemIndex != 0) {
            var removeButton = new Button("X");
            removeButton.setOnAction(actionEvent -> eventHandler.accept(this, null));
            var borderTitle = new BorderPane();
            borderTitle.setLeft(titleLabel);
            borderTitle.setRight(removeButton);
            titleNode = borderTitle;
        }
        else titleNode = titleLabel;

        descriptionArea = new Label(item.getExtendedDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setAlignment(Pos.TOP_LEFT);
        descriptionArea.setId(makeAnId("Description"));

        whichPlugin = new Label("Plugin loading");
        whichPlugin.setStyle("-fx-font-size: 90%;");
        whichPlugin.setId(makeAnId("WhichPlugin"));

        licenseLink = new Hyperlink("License unknown");
        licenseLink.setDisable(true);
        licenseLink.setStyle("-fx-font-size: 90%;");

        vendorLink = new Hyperlink("Vendor unknown");
        vendorLink.setDisable(true);
        vendorLink.setStyle("-fx-font-size: 90%;");

        docsLink = new Hyperlink("No Docs");
        docsLink.setDisable(true);
        docsLink.setStyle("-fx-font-size: 90%;");
        docsLink.setId(makeAnId("Docs"));

        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.TOP_LEFT);
        infoContainer.getChildren().add(descriptionArea);
        infoContainer.getChildren().add(whichPlugin);
        infoContainer.getChildren().add(docsLink);
        infoContainer.getChildren().add(licenseLink);
        infoContainer.getChildren().add(vendorLink);

        actionButton = new Button(action == UICodeAction.CHANGE ? "Change" : "Select");
        if(BaseDialogSupport.isCurrentThemeDark()) {
            actionButton.setStyle("-fx-font-size: 110%; -fx-font-weight: bold; -fx-background-color: #444");
        }
        else {
            actionButton.setStyle("-fx-font-size: 110%; -fx-font-weight: bold; -fx-background-color: #aeb2bd");
        }
        actionButton.setMaxSize(2000, 2000);
        actionButton.setOnAction(event-> eventHandler.accept(this, item));
        actionButton.setId(makeAnId("ActionButton"));

        setImageButton(item);

        setTop(titleNode);
        BorderPane.setMargin(titleLabel, new Insets(5.0));

        VBox buttonBox = new VBox(actionButton);
        setLeft(buttonBox);
        BorderPane.setMargin(buttonBox, new Insets(5.0));

        setCenter(infoContainer);
        BorderPane.setMargin(infoContainer, new Insets(5.0));

        propertiesPanel = new VBox(3);
        if(showProperties) propertiesPanel.setPrefWidth(300);
        setRight(propertiesPanel);
        BorderPane.setMargin(propertiesPanel, new Insets(5.0));

        setItem(item);
    }

    private String makeAnId(String title) {
        var myId = getId();
        if(StringHelper.isStringEmptyOrNull(myId)) myId = "unknownPlugin";
        return myId + title;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int i) {
        itemIndex = i;
    }

    private void prepareProperties() {
        propertiesPanel.getChildren().clear();
        if(!showProperties) return;

        Node uiNodeToAdd;
        for(var property : item.getProperties()) {
            if (property.getValidationRules() instanceof BooleanPropertyValidationRules) {
                uiNodeToAdd = generateBooleanCheckBoxField(property);
            }
            else {
                propertiesPanel.getChildren().add(new Label(property.getDescription()));
                if (property.getValidationRules().hasChoices()) {
                    uiNodeToAdd = generateRegularComboField(property);
                } else if (property.getValidationRules() instanceof IoExpanderPropertyValidationRules ioRules) {
                    ioRules.initialise(editorUI.getCurrentProject());
                    uiNodeToAdd = generateIoExpanderField(propertiesPanel, property, ioRules);
                } else if (property.getValidationRules() instanceof FontPropertyValidationRules) {
                    uiNodeToAdd = generateFontField(propertiesPanel, property);
                } else {
                    uiNodeToAdd = generateTextField(property);
                }
            }
            propertiesPanel.getChildren().add(uiNodeToAdd);
            propertiesById.put(property.getName(), new PropertyWithControl(property, uiNodeToAdd));
        }

        evaluateAllEditorFieldsAgainstProps();
    }

    private CheckBox generateBooleanCheckBoxField(CreatorProperty property) {
        CheckBox checkbox = new CheckBox(property.getDescription());
        checkbox.setSelected(Boolean.parseBoolean(property.getLatestValue()));
        checkbox.setOnAction(actionEvent -> commitEdit(property, Boolean.toString(checkbox.isSelected())));
        checkbox.setOpaqueInsets(new Insets(3, 0, 3, 0));
        checkbox.setTooltip(new Tooltip(property.getExtendedDescription()));
        checkbox.setId(makeAnId(property.getName()));
        return checkbox;
    }

    private void setImageButton(CodePluginItem item) {
        mgr.getImageForName(item, item.getImageFileName())
                .ifPresent(img -> {
                    double scaleFactor = img.getWidth() / IMG_THUMB_WIDTH;
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(IMG_THUMB_WIDTH);
                    imgView.setFitHeight(img.getHeight() / scaleFactor);
                    actionButton.setGraphic(imgView);
                    actionButton.setContentDisplay(ContentDisplay.TOP);
                });
    }

    private Node generateIoExpanderField(VBox propertiesPanel, CreatorProperty property, IoExpanderPropertyValidationRules ioRules) {
        HBox hBox = new HBox(2);
        TextField expanderLabel = new TextField(ioRules.getNameOfCurrentChoice(property.getLatestValue()));
        expanderLabel.setTooltip(new Tooltip(property.getExtendedDescription()));
        expanderLabel.setDisable(true);
        expanderLabel.setId(makeAnId(property.getName()));
        Button fontButton = new Button("Choose IO");
        fontButton.setTooltip(new Tooltip(property.getExtendedDescription()));
        fontButton.setId(makeAnId(property.getName() + "_btn"));
        fontButton.setOnAction(actionEvent -> {
            Stage scene = (Stage) propertiesPanel.getScene().getWindow();
            IoExpanderDefinitionCollection expanderDefinitions = editorUI.getCurrentProject().getGeneratorOptions().getExpanderDefinitions();
            var dialog = new ChooseIoExpanderDialog(scene, expanderDefinitions.getDefinitionById(property.getLatestValue()),
                    editorUI.getCurrentProject(), true);
            dialog.getResultOrEmpty().ifPresent(ioExpanderId -> {
                commitEdit(property, ioExpanderId);
                expanderLabel.setText(ioRules.getNameOfCurrentChoice(ioExpanderId));
            });
        });
        hBox.getChildren().add(expanderLabel);
        hBox.getChildren().add(fontButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }

    private Node generateFontField(VBox propertiesPanel, CreatorProperty property) {
        HBox hBox = new HBox(2);
        TextField fontLabel = new TextField(nicePrintableFontName(property.getLatestValue()));
        fontLabel.setTooltip(new Tooltip(property.getExtendedDescription()));
        fontLabel.setDisable(true);
        fontLabel.setId(makeAnId(property.getName()));
        Button fontButton = new Button("Set Font");
        fontButton.setTooltip(new Tooltip(property.getExtendedDescription()));
        fontButton.setId(makeAnId(property.getName() + "_btn"));
        fontButton.setOnAction(actionEvent -> {
            Stage scene = (Stage) propertiesPanel.getScene().getWindow();
            ChooseFontDialog dialog = new ChooseFontDialog(scene, property.getLatestValue(), true);
            dialog.getResultOrEmpty().ifPresent(fontAsString -> {
                commitEdit(property, fontAsString);
                fontLabel.setText(nicePrintableFontName(fontAsString));
            });
        });
        hBox.getChildren().add(fontLabel);
        hBox.getChildren().add(fontButton);
        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }

    private String nicePrintableFontName(String latestValue) {
        var def = FontDefinition.fromString(latestValue);
        if(def.isPresent()) {
            return def.get().getNicePrintableName();
        }
        else return latestValue;
    }

    private ComboBox<ChoiceDescription> generateRegularComboField(CreatorProperty property) {
        ComboBox<ChoiceDescription> comboBox;

        if(property.getValidationRules() instanceof MenuItemValidationRules valRules) {
            valRules.initialise(new ArrayList<>(allItems));
        }

        comboBox = new ComboBox<>(FXCollections.observableList(property.getValidationRules().choices()));
        var currentChoice = property.getValidationRules().getChoiceFor(property.getLatestValue());

        if(currentChoice != null)
            comboBox.getSelectionModel().select(currentChoice);
        else
            comboBox.getSelectionModel().select(0);

        comboBox.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean old, Boolean newVal) -> {
            if (!newVal) {
                commitEdit(property, comboBox.getValue().getChoiceValue());
            }
        });
        comboBox.setOnAction(actionEvent -> commitEdit(property, comboBox.getValue().getChoiceValue()));
        comboBox.setTooltip(new Tooltip(property.getExtendedDescription()));
        comboBox.setId(makeAnId(property.getName()));

        return comboBox;
    }

    private TextField generateTextField(CreatorProperty property) {
        var textField = new TextField(property.getLatestValue());
        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean old, Boolean newVal) -> {
            if (!newVal) {
                commitEdit(property, textField.getText());
            }
        });
        textField.setOnAction(actionEvent -> commitEdit(property, textField.getText()));
        textField.setTooltip(new Tooltip(property.getExtendedDescription()));
        textField.setId(makeAnId(property.getName()));
        return textField;
    }

    private void commitEdit(CreatorProperty property, String value) {
        if(property.getValidationRules().isValueValid(value)) {
            property.setLatestValue(value);
            evaluateAllEditorFieldsAgainstProps();
        }
        else if(editorUI != null) {
            editorUI.alertOnError(
                    "Validation error during table edit",
                    "The value '" + value + "' is not valid for " + property.getName()
                            + "\nReason: " + property.getValidationRules());
        }
    }

    private void evaluateAllEditorFieldsAgainstProps() {
        propertiesById.values().forEach(prop -> {
            CodeApplicability applicability = prop.property().getApplicability();
            prop.control().setDisable(!applicability.isApplicable(item.getProperties()));
        });
    }

    public void setItem(CodePluginItem item) {
        this.item = item;

        descriptionArea.setText(item.getExtendedDescription());
        titleLabel.setText(item.getDescription());

        if(item.getDocsLink() != null) {
            docsLink.setText("Click for documentation");
            docsLink.setDisable(false);
            docsLink.setOnAction((event)->SafeNavigator.safeNavigateTo(item.getDocsLink()));
        }

        setImageButton(item);
        prepareProperties();

        var config = item.getConfig();
        whichPlugin.setText(config.getName() + " - " + config.getVersion());
        licenseLink.setText(config.getLicense());
        licenseLink.setDisable(false);
        licenseLink.setOnAction((event)-> SafeNavigator.safeNavigateTo(config.getLicenseUrl()));
        if(config.getVendor() != null) {
            vendorLink.setText(config.getVendor());
            vendorLink.setDisable(false);
            vendorLink.setOnAction((event) -> SafeNavigator.safeNavigateTo(config.getVendorUrl()));
        }
    }

    public CodePluginItem getItem() {
        return item;
    }

    record PropertyWithControl(CreatorProperty property, Node control) {}
}
