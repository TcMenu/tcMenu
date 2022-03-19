package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JfxHeaderBar implements TitleWidgetListener<Image> {
    private GridPane grid;
    private Label titleArea;
    private final List<TitleWidget<Image>> titleWidgets;
    private final Map<TitleWidget<Image>, Button> widgetButtonMap = new HashMap<>();
    private Button leftButton;
    private Runnable leftButtonFunction = null;

    public JfxHeaderBar(List<TitleWidget<Image>> titleWidgets) {
        this.titleWidgets = titleWidgets;
    }

    public Node initialiseControls() {
        grid = new GridPane();
        titleArea = new Label("");
        titleArea.setMaxWidth(Double.MAX_VALUE);
        titleArea.setStyle("-fx-font-size: 18px;-fx-text-alignment: left;");
        grid.setPadding(new Insets(4));
        grid.getColumnConstraints().clear();
        grid.getColumnConstraints().addAll(
                new ColumnConstraints(25, 25, 120, Priority.ALWAYS, HPos.LEFT, false),
                new ColumnConstraints(100, 800, Double.MAX_VALUE, Priority.ALWAYS, HPos.LEFT, true),
                new ColumnConstraints(32, 140, Double.MAX_VALUE, Priority.ALWAYS, HPos.RIGHT, false)
        );
        leftButton = new Button();
        leftButton.setOnAction(e -> {
            if(leftButtonFunction != null) leftButtonFunction.run();
        });
        hideLeftButton();
        leftButton.setStyle("-fx-background-color: #295c95;");
        grid.add(leftButton, 0, 0);
        grid.add(titleArea, 1, 0);
        grid.setStyle("-fx-background-color: #0f7ab0");
        var widgetPane = new HBox(4);
        for(var wid : titleWidgets) {
            var widgetBtn = new Button();
            widgetBtn.setStyle("-fx-background-color: #295c95;");
            var img = wid.getCurrentImage();
            double scaleFactor = img.getWidth() / 16;
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(16);
            imgView.setFitHeight(img.getHeight() / scaleFactor);
            widgetBtn.setGraphic(imgView);
            widgetPane.getChildren().add(widgetBtn);
            widgetButtonMap.put(wid, widgetBtn);
            wid.addWidgetChangeListener(this);
        }
        grid.add(widgetPane, 2, 0);
        return grid;
    }

    public void showLeftButton(String text) {
        leftButton.setText(text);
        leftButton.setManaged(true);
        leftButton.setVisible(true);
    }

    public void hideLeftButton() {
        leftButton.setManaged(false);
        leftButton.setVisible(false);
    }

    public void titleChanged(String newTitle) {
        titleArea.setText(newTitle);
    }

    @Override
    public void titleWidgetHasChanged(TitleWidget<Image> widget) {
        Platform.runLater(() -> {
            var widGfx = widgetButtonMap.get(widget);
            var imgView = (ImageView)widGfx.getGraphic();
            imgView.setImage(widget.getCurrentImage());
        });
    }

    public static TitleWidget<Image> widgetFromImages(URL... imageName) {
        var images = Arrays.stream(imageName)
                .map(i -> new Image(i.toString()))
                .toList();
        return new TitleWidget<>(images, images.size(), 0);
    }

    public void setLeftButtonFunction(Runnable f) {
        leftButtonFunction = f;
    }
}
