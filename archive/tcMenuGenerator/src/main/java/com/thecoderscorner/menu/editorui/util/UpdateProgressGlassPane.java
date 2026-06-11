package com.thecoderscorner.menu.editorui.util;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;

import java.util.Objects;

public class UpdateProgressGlassPane extends Popup {

    private final BorderPane pane;
    private final Button closeBtn;
    private ProgressIndicator progress;
    private Label progressInfo;
    private StackPane parent;

    public UpdateProgressGlassPane() {
        setAutoHide(false);
        setHideOnEscape(false);

        progress = new ProgressIndicator(0.0);
        progress.setProgress(0.0);
        progress.setMinWidth(150);
        progress.setMinHeight(150);

        progressInfo = new Label("Please wait..");
        progressInfo.setAlignment(Pos.CENTER_LEFT);
        progressInfo.setPrefWidth(400);
        progressInfo.setWrapText(true);

        pane = new BorderPane();
        pane.getStyleClass().add("background");
        pane.setStyle("-fx-border-width: 2px;");
        pane.setStyle("-fx-border-color: #555;");
        pane.setPadding(new Insets(16.0));
        pane.setCenter(progressInfo);
        pane.setLeft(progress);

        closeBtn = new Button("Close");
        closeBtn.setOnAction(e -> hide());
        closeBtn.setDisable(true);

        ButtonBar bar = new ButtonBar();
        bar.getButtons().add(closeBtn);
        pane.setBottom(bar);

        getContent().add(pane);
    }

    public void updateProgress(double howFarThrough, String textUpdate) {
        Platform.runLater(() -> {
            progress.setProgress(howFarThrough);
            progressInfo.setText(textUpdate);
        });
    }

    public void completed(boolean success, String text) {
        Platform.runLater(() -> {
            closeBtn.setCancelButton(true);
            progressInfo.setText(text);
            progress.setProgress(success ? 1.0 : 0.0);
            closeBtn.setDisable(false);
        });
    }
}
