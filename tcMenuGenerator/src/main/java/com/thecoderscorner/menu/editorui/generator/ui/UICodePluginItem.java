/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.dialog.ChooseFontDialog;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.parameters.FontDefinition;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;
import com.thecoderscorner.menu.editorui.generator.validation.ChoicesPropertyValidationRules;
import com.thecoderscorner.menu.editorui.generator.validation.FontPropertyValidationRules;
import com.thecoderscorner.menu.editorui.generator.validation.MenuItemValidationRules;
import com.thecoderscorner.menu.editorui.uimodel.CurrentProjectEditorUI;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

public class UICodePluginItem extends BorderPane {

    public static final int IMG_THUMB_WIDTH = 150;

    public enum UICodeAction { CHANGE, SELECT }

    private final boolean showProperties;
    private final Consumer<CodePluginItem> eventHandler;
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
    private final static System.Logger LOGGER = System.getLogger(UICodePluginItem.class.getSimpleName());

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action, Consumer<CodePluginItem> evt) {
        this(mgr, item, action, evt, null, null);
    }

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action, Consumer<CodePluginItem> evt,
                            CurrentProjectEditorUI editorUI, Collection<com.thecoderscorner.menu.domain.MenuItem> allItems) {
        super();
        showProperties = allItems != null && editorUI != null;

        this.editorUI = editorUI;
        this.eventHandler = evt;

        this.mgr = mgr;
        this.item = item;

        titleLabel = new Label(item.getDescription());
        this.allItems = allItems;
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");

        descriptionArea = new Label(item.getExtendedDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setAlignment(Pos.TOP_LEFT);
        descriptionArea.setPrefWidth(1900);

        whichPlugin = new Label("Plugin loading");
        whichPlugin.setStyle("-fx-font-size: 90%;");

        licenseLink = new Hyperlink("License unknown");
        licenseLink.setDisable(true);
        licenseLink.setStyle("-fx-font-size: 90%;");

        vendorLink = new Hyperlink("Vendor unknown");
        vendorLink.setDisable(true);
        vendorLink.setStyle("-fx-font-size: 90%;");

        docsLink = new Hyperlink("No Docs");
        docsLink.setDisable(true);
        docsLink.setStyle("-fx-font-size: 90%;");

        VBox infoContainer = new VBox(5);
        infoContainer.setAlignment(Pos.TOP_LEFT);
        infoContainer.getChildren().add(descriptionArea);
        infoContainer.getChildren().add(whichPlugin);
        infoContainer.getChildren().add(docsLink);
        infoContainer.getChildren().add(licenseLink);
        infoContainer.getChildren().add(vendorLink);

        actionButton = new Button(action == UICodeAction.CHANGE ? "Change" : "Select");
        actionButton.setStyle("-fx-font-size: 110%; -fx-font-weight: bold; -fx-background-color: #d4d9fd");
        actionButton.setMaxSize(2000, 2000);
        actionButton.setOnAction(event-> eventHandler.accept(item));

        setImageButton(item);

        setTop(titleLabel);
        BorderPane.setMargin(titleLabel, new Insets(5.0));

        setLeft(new VBox(actionButton));
        BorderPane.setMargin(actionButton, new Insets(5.0));

        setCenter(infoContainer);
        BorderPane.setMargin(infoContainer, new Insets(5.0));

        propertiesPanel = new VBox();
        if(showProperties) propertiesPanel.setPrefWidth(300);
        setRight(propertiesPanel);
        BorderPane.setMargin(propertiesPanel, new Insets(5.0));

        setItem(item);
    }

    private void prepareProperties() {
        propertiesPanel.getChildren().clear();
        if(!showProperties) return;

        item.getProperties().forEach(property -> {
            propertiesPanel.getChildren().add(new Label(property.getDescription()));
            if(property.getValidationRules().hasChoices()) {
                ComboBox<ChoiceDescription> comboBox = generateRegularComboField(property);
                propertiesPanel.getChildren().add(comboBox);
            }
            else if(property.getValidationRules() instanceof FontPropertyValidationRules) {
                generateFontField(propertiesPanel, property);
            }
            else {
                generateTextField(propertiesPanel, property);
            }
        });

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

    private void generateFontField(VBox propertiesPanel, CreatorProperty property) {
        HBox hBox = new HBox(2);
        TextField fontLabel = new TextField(nicePrintableFontName(property.getLatestValue()));
        fontLabel.setDisable(true);
        Button fontButton = new Button("Set Font");
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
        propertiesPanel.getChildren().add(hBox);
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

        if(property.getValidationRules() instanceof MenuItemValidationRules) {
            var valRules = (MenuItemValidationRules) property.getValidationRules();
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
        return comboBox;
    }

    private void generateTextField(VBox propertiesPanel, CreatorProperty property) {
        var textField = new TextField(property.getLatestValue());
        propertiesPanel.getChildren().add(textField);
        textField.focusedProperty().addListener((ObservableValue<? extends Boolean> o, Boolean old, Boolean newVal) -> {
            if (!newVal) {
                commitEdit(property, textField.getText());
            }
        });
        textField.setOnAction(actionEvent -> commitEdit(property, textField.getText()));
    }

    private void commitEdit(CreatorProperty property, String value) {
        if(property.getValidationRules().isValueValid(value)) {
            property.setLatestValue(value);
        }
        else if(editorUI != null) {
            editorUI.alertOnError(
                    "Validation error during table edit",
                    "The value '" + value + "' is not valid for " + property.getName()
                            + "\nReason: " + property.getValidationRules());
        }
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
}
