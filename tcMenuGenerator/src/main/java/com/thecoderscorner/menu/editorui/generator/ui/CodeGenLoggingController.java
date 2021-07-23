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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.LinkedList;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator.LINE_BREAK;


public class CodeGenLoggingController {
    public Button closeButton;
    public Button copyButton;
    public Button includeDebugButton;
    public ListView<LogLine> loggerList;
    private final LinkedList<LogLine> allLogEntries = new LinkedList<>();
    private final StringBuilder loggingTextBuilder = new StringBuilder(1024);
    private boolean debugIsEnabled = false;

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
            LogLine logLine = new LogLine(text, level);
                allLogEntries.addFirst(logLine);
            if(level != System.Logger.Level.DEBUG || debugIsEnabled) {
                loggingTextBuilder.append(text).append(LINE_BREAK);
                loggerList.getItems().add(0, logLine);
            }
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

    public void onIncludeDebug(ActionEvent actionEvent) {
        debugIsEnabled = !debugIsEnabled;
        includeDebugButton.setText(debugIsEnabled ? "Remove debug" : "Include debug");
        loggerList.getItems().clear();
        if(debugIsEnabled) {
            loggerList.getItems().addAll(allLogEntries);
        }
        else {
            loggerList.getItems().addAll(allLogEntries.stream()
                    .filter(logLine -> logLine.getLevel() != System.Logger.Level.DEBUG)
                    .toList());
        }
    }

    static class ColorLogLineCell extends ListCell<LogLine> {

        ColorLogLineCell() {
            setStyle("-fx-padding: 0px;");
        }

        @Override
        public void updateItem(LogLine item, boolean empty) {
            super.updateItem(item, empty);
            if (item == null) return;
            Label lbl = new Label(item.getLevel().toString());
            Color bgCol = Color.AQUA;
            Color fgCol = Color.BLACK;
            switch(item.getLevel()) {
                case ALL:
                case TRACE:
                case DEBUG:
                    bgCol = Color.GRAY;
                    fgCol = Color.BLACK;
                    break;
                case INFO:
                    bgCol = Color.GREEN;
                    fgCol = Color.WHITE;
                    break;
                case WARNING:
                    bgCol = Color.ORANGE;
                    fgCol = Color.BLACK;
                    break;
                case ERROR:
                case OFF:
                    bgCol = Color.RED;
                    fgCol = Color.WHITE;
                    break;
            }
            lbl.setBackground(new Background(new BackgroundFill(bgCol, CornerRadii.EMPTY, Insets.EMPTY)));
            lbl.setTextFill(fgCol);
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
