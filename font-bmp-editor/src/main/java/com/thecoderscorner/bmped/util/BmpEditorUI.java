package com.thecoderscorner.bmped.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.Optional;

public interface BmpEditorUI {

    Optional<String> saveFileWithChooser(String extensions, Optional<String> nameSuggestion, byte[] data);
    Optional<FileNameAndData> openFileWithChooser(String extensions);

    public boolean questionYesNo(String title, String text);
    void alertOnError(String errorTitle, String errorText);
    void showAlertAndWait(Alert.AlertType alertType, String title, String text, ButtonType buttonType);

    record FileNameAndData(String fileName, byte[] data) {}
}
