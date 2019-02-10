/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import java.awt.*;
import java.net.URI;

import static java.lang.System.Logger.Level.ERROR;

public class UICodePluginItem extends BorderPane {
    public enum UICodeAction { CHANGE, SELECT;}

    private final UICodeAction action;
    private BorderPane innerBorder;
    private Node imagePanel;
    private Label titleLabel;
    private Label descriptionArea;
    private HBox infoContainer;
    private Label whichPlugin;
    private Hyperlink licenseLink;
    private CodePluginManager mgr;
    private CodePluginItem item;
    private Button actionButton;
    private final static System.Logger LOGGER = System.getLogger(UICodePluginItem.class.getSimpleName());

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action, EventHandler<ActionEvent> evt) {
        super();

        this.action = action;

        this.mgr = mgr;
        this.item = item;

        titleLabel = new Label(item.getDescription());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 110%;");

        descriptionArea = new Label(item.getExtendedDescription());
        descriptionArea.setWrapText(true);
        descriptionArea.setAlignment(Pos.TOP_LEFT);
        descriptionArea.setPrefWidth(1900);

        whichPlugin = new Label("Plugin loading");
        whichPlugin.setStyle("-fx-font-size: 90%;");
        whichPlugin.setPadding(new Insets(10, 5, 5, 5));

        licenseLink = new Hyperlink("Not set");
        licenseLink.setPadding(new Insets(10, 0, 5, 0));
        licenseLink.setStyle("-fx-font-size: 90%;");

        infoContainer = new HBox(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.getChildren().add(whichPlugin);
        infoContainer.getChildren().add(licenseLink);

        innerBorder = new BorderPane();
        innerBorder.setPadding(new Insets(4));
        innerBorder.setTop(titleLabel);
        innerBorder.setCenter(descriptionArea);
        innerBorder.setBottom(infoContainer);

        actionButton = new Button(action == UICodeAction.CHANGE ? "Change" : "Select");
        actionButton.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        actionButton.setMaxSize(2000, 2000);
        actionButton.setOnAction(evt);

        setRight(actionButton);
        setCenter(innerBorder);

        setItem(item);
    }

    public void setItem(CodePluginItem item) {
        this.item = item;

        descriptionArea.setText(item.getExtendedDescription());
        titleLabel.setText(item.getDescription());

        mgr.getPluginConfigForItem(item).ifPresentOrElse(config -> {
            whichPlugin.setText(config.getName() + " - " + config.getVersion());
            licenseLink.setText(config.getLicense());
            licenseLink.setDisable(false);
            licenseLink.setOnAction((event)-> {
                try {
                    Desktop.getDesktop().browse(new URI(config.getLicenseUrl()));
                } catch (Exception e) {
                    LOGGER.log(ERROR,"Unable to locate license URL" + config.getLicenseUrl());
                }
            });
        }, ()->{
            whichPlugin.setText("Unknown plugin");
            licenseLink.setText("Unknown plugin");
            licenseLink.setDisable(true);
        });

        imagePanel = mgr.getImageForName(item.getImageFileName())
                .map(img -> {
                    double scaleFactor = img.getHeight() / 80;
                    ImageView imgView = new ImageView(img);
                    imgView.setFitHeight(80);
                    imgView.setFitWidth(img.getWidth() / scaleFactor);
                    return (Node)imgView;
                })
                .orElseGet(()-> {
                    Label noImg = new Label("No Image");
                    noImg.setAlignment(Pos.CENTER);
                    noImg.setMaxSize(150, 80);
                    noImg.setStyle("-fx-background-color: #e0ccff;-fx-fill: #000000;");
                    return (Node)noImg;
                });
        setLeft(imagePanel);
    }

    public CodePluginItem getItem() {
        return item;
    }
}
