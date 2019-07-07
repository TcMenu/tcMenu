/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
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
import java.util.function.Consumer;

import static java.lang.System.Logger.Level.ERROR;

public class UICodePluginItem extends BorderPane {

    public static final int IMG_THUMB_WIDTH = 150;

    public enum UICodeAction { CHANGE, SELECT;}

    private Consumer<CodePluginItem> eventHandler;
    private BorderPane innerBorder;
    private Node imagePanel;
    private Label titleLabel;
    private Label descriptionArea;
    private HBox infoContainer;
    private Label whichPlugin;
    private Hyperlink licenseLink;
    private Hyperlink vendorLink;
    private Hyperlink docsLink;
    private CodePluginManager mgr;
    private CodePluginItem item;
    private Button actionButton;
    private final static System.Logger LOGGER = System.getLogger(UICodePluginItem.class.getSimpleName());

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item, UICodeAction action, Consumer<CodePluginItem> evt) {
        super();

        this.eventHandler = evt;

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

        licenseLink = new Hyperlink("License unknown");
        licenseLink.setDisable(true);
        licenseLink.setPadding(new Insets(10, 0, 5, 0));
        licenseLink.setStyle("-fx-font-size: 90%;");

        vendorLink = new Hyperlink("Vendor unknown");
        vendorLink.setDisable(true);
        vendorLink.setPadding(new Insets(10, 0, 5, 0));
        vendorLink.setStyle("-fx-font-size: 90%;");

        docsLink = new Hyperlink("No Docs");
        docsLink.setDisable(true);
        docsLink.setPadding(new Insets(10, 0, 5, 0));
        docsLink.setStyle("-fx-font-size: 90%;");

        infoContainer = new HBox(5);
        infoContainer.setAlignment(Pos.CENTER_LEFT);
        infoContainer.getChildren().add(whichPlugin);
        infoContainer.getChildren().add(docsLink);
        infoContainer.getChildren().add(licenseLink);
        infoContainer.getChildren().add(vendorLink);

        innerBorder = new BorderPane();
        innerBorder.setPadding(new Insets(4));
        innerBorder.setTop(titleLabel);
        innerBorder.setCenter(descriptionArea);
        innerBorder.setBottom(infoContainer);

        actionButton = new Button(action == UICodeAction.CHANGE ? "Change" : "Select");
        actionButton.setStyle("-fx-font-size: 110%; -fx-font-weight: bold;");
        actionButton.setMaxSize(2000, 2000);
        actionButton.setOnAction(event-> eventHandler.accept(item));

        setRight(actionButton);
        setCenter(innerBorder);

        setItem(item);
    }

    public void setItem(CodePluginItem item) {
        this.item = item;

        descriptionArea.setText(item.getExtendedDescription());
        titleLabel.setText(item.getDescription());

        if(item.getDocsLink() != null) {
            docsLink.setText("Click for documentation");
            docsLink.setDisable(false);
            docsLink.setOnAction((event)-> {
                try {
                    Desktop.getDesktop().browse(new URI(item.getDocsLink()));
                } catch (Exception e) {
                    LOGGER.log(ERROR,"Unable to locate docs URL" + item.getDocsLink());
                }
            });
        }

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
            if(config.getVendor() != null) {
                vendorLink.setText(config.getVendor());
                vendorLink.setDisable(false);
                vendorLink.setOnAction((event) -> {
                    try {
                        Desktop.getDesktop().browse(new URI(config.getVendorUrl()));
                    } catch (Exception e) {
                        LOGGER.log(ERROR, "Unable to locate vendor URL" + config.getVendorUrl());
                    }
                });
            }
        }, ()->{
            whichPlugin.setText("Unknown plugin");
            licenseLink.setText("Unknown plugin");
            licenseLink.setDisable(true);
            vendorLink.setText("Unknown vendor");
            vendorLink.setDisable(true);
        });

        imagePanel = mgr.getImageForName(item.getImageFileName())
                .map(img -> {
                    double scaleFactor = img.getWidth() / IMG_THUMB_WIDTH;
                    ImageView imgView = new ImageView(img);
                    imgView.setFitWidth(IMG_THUMB_WIDTH);
                    imgView.setFitHeight(img.getHeight() / scaleFactor);
                    return (Node)imgView;
                })
                .orElseGet(()-> {
                    Label noImg = new Label("No Image");
                    noImg.setAlignment(Pos.CENTER);
                    noImg.setPrefSize(IMG_THUMB_WIDTH, IMG_THUMB_WIDTH / 2.0);
                    noImg.setStyle("-fx-background-color: #e0ccff;-fx-fill: #000000;");
                    return noImg;
                });
        setLeft(imagePanel);
    }

    public CodePluginItem getItem() {
        return item;
    }
}
