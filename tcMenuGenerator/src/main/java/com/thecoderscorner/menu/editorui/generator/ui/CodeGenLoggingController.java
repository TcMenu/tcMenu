/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.ui;

import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public class CodeGenLoggingController {
    public Button closeButton;
    public Button copyButton;
    public TextArea loggingArea;

    public void init(Path dir, List<EmbeddedCodeCreator> generators, MenuTree tree) {
        ArduinoGenerator generator = new ArduinoGenerator(this::logLine, dir, generators, tree);
        closeButton.setDisable(true);
        Thread th = new Thread(() -> {
            generator.startConversion();
            Platform.runLater(() -> closeButton.setDisable(false));
        });
        th.start();
    }

    private void logLine(String s) {
        Platform.runLater(()->
                loggingArea.appendText(s + LINE_BREAK)
        );
    }

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
}
