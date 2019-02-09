package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

public class UICodePluginItem extends BorderPane {
    private BorderPane innerBorder;
    private Node imagePanel;
    private Label titleLabel;
    private Label descriptionArea;
    private Label whichPlugin;
    private CodePluginManager mgr;
    private CodePluginItem item;

    public UICodePluginItem(CodePluginManager mgr, CodePluginItem item) {
        super();
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

        innerBorder = new BorderPane();
        innerBorder.setPadding(new Insets(4));
        innerBorder.setTop(titleLabel);
        innerBorder.setCenter(descriptionArea);
        innerBorder.setBottom(whichPlugin);

        setCenter(innerBorder);

        setItem(item);
    }

    public void setItem(CodePluginItem item) {
        this.item = item;

        descriptionArea.setText(item.getExtendedDescription());
        titleLabel.setText(item.getDescription());

        var pluginVer = mgr.getPluginConfigForItem(item)
                .map(cfg-> cfg.getName() + " - " + cfg.getVersion())
                .orElse("Plugin information not found");
        whichPlugin.setText("Plugin information: " + pluginVer);

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
}
