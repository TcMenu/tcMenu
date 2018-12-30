/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.editorui.generator.CodeGenerator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public class CodeGenLoggingController {
    public Button closeButton;
    public Button copyButton;
    public TextArea loggingArea;

    /**
     * initialises the code generator controller by setting off the conversion and stopping exit until
     * it has completed. Runs the potentially long running converter on its own thread.
     * @param generator the code generator in use.
     */
    public void init(CodeGenerator generator) {
        generator.setLoggerFunction(this::logLine);
        closeButton.setDisable(true);
    }

    /**
     * This is to allow the logger to write into our log output area.
     * @param s the log line
     */
    private void logLine(String s) {
        Platform.runLater(()->
                loggingArea.appendText(s + LINE_BREAK)
        );
    }

    /**
     * A shortcut button to get the contents of the logger window into the clipboard.
     * @param actionEvent ignored
     */
    public void onCopyToClipboard(ActionEvent actionEvent) {
        Clipboard systemClipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(loggingArea.getText());
        systemClipboard.setContent(content);
    }

    public void onClose(ActionEvent actionEvent) {
        Stage s = (Stage) loggingArea.getScene().getWindow();
        s.close();
    }

    public void enableCloseButton() {
        closeButton.setDisable(false);
    }
}
