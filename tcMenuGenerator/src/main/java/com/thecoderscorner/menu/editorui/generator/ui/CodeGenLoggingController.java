/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator.LINE_BREAK;


public class CodeGenLoggingController {
    public Button closeButton;
    public Button copyButton;
    public ListView<LogLine> loggerList;
    private final StringBuilder loggingTextBuilder = new StringBuilder(1024);

    /**
     * initialises the code generator controller by setting off the conversion and stopping exit until
     * it has completed. Runs the potentially long running converter on its own thread.
     * @param generator the code generator in use.
     */
    public void init(CodeGenerator generator) {
        generator.setLoggerFunction(this::logLine);
        closeButton.setDisable(true);

        loggerList.setCellFactory(list -> new ColorLogLineCell());
    }

    /**
     * This is to allow the logger to write into our log output area.
     * @param text the log line
     * @param level the level to log at
     */
    private void logLine(System.Logger.Level level, String text) {
        Platform.runLater(()-> {
            loggingTextBuilder.append(text).append(LINE_BREAK);
            loggerList.getItems().add(0, new LogLine(text, level));
        });
    }

    /**
     * A shortcut button to get the contents of the logger window into the clipboard.
     * @param actionEvent ignored
     */
    public void onCopyToClipboard(ActionEvent actionEvent) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(loggingTextBuilder.toString());
        systemClipboard.setContent(content);
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) closeButton.getScene().getWindow();
        s.close();
    }

    public void enableCloseButton() {
        closeButton.setDisable(false);
    }

    static class ColorLogLineCell extends ListCell<LogLine> {
        @Override
        public void updateItem(LogLine item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) return;
            Label lbl = new Label(item.getLevel().toString());
            Color bgCol = Color.AQUA;
            switch(item.getLevel()) {
                case ALL:
                case TRACE:
                case DEBUG:
                case INFO:
                    bgCol = Color.GREEN;
                    break;
                case WARNING:
                    bgCol = Color.YELLOW;
                    break;
                case ERROR:
                case OFF:
                    bgCol = Color.RED;
                    break;
            }
            lbl.setBackground(new Background(new BackgroundFill(bgCol, CornerRadii.EMPTY, Insets.EMPTY)));
            lbl.setPrefWidth(100);
            Label txtLbl = new Label(item.getText());
            HBox hBox = new HBox(0);
            hBox.getChildren().add(lbl);
            hBox.getChildren().add(txtLbl);
            setGraphic(hBox);
            setPadding(Insets.EMPTY);
        }
    }
}
