package com.thecoderscorner.bmped.util;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocalBmpEditorUI implements BmpEditorUI {
    private final Logger logger = Logger.getLogger(getClass().getSimpleName());
    private final Stage mainStage;

    public LocalBmpEditorUI(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public Optional<String> saveFileWithChooser(String extensions, Optional<String> nameSuggestion, byte[] data) {
        try {
            var fileNameOpt = findFileNameFromUser(Optional.empty(), nameSuggestion, false, extensions);
            if (fileNameOpt.isEmpty()) {
                return Optional.empty();
            }
            Files.write(Path.of(fileNameOpt.get()), data);
            return fileNameOpt;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error saving file", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<FileNameAndData> openFileWithChooser(String extensions) {
        try {
            var fileNameOpt = findFileNameFromUser(Optional.empty(), Optional.empty(), true, extensions);
            if (fileNameOpt.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.of(new FileNameAndData(fileNameOpt.get(), Files.readAllBytes(Path.of(fileNameOpt.get()))));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error opening file", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean questionYesNo(String title, String header) {
        var btn = AlertUtil.showAlertAndWait(Alert.AlertType.CONFIRMATION, title, header, ButtonType.YES, ButtonType.NO);
        return btn.orElse(ButtonType.NO) == ButtonType.YES;
    }

    @Override
    public void alertOnError(String heading, String description) {
        showAlertAndWait(Alert.AlertType.ERROR, heading, description, ButtonType.CLOSE);

    }

    @Override
    public void showAlertAndWait(Alert.AlertType alertType, String title, String text, ButtonType buttonType) {
        AlertUtil.showAlertAndWait(alertType, title, text, buttonType);
    }

    public Optional<String> findFileNameFromUser(Optional<Path> initialDir, Optional<String> suggestedName, boolean open, String allowedExtensions) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a File");
        initialDir.ifPresentOrElse(
                dir -> fileChooser.setInitialDirectory(new File(dir.toString())),
                () -> fileChooser.setInitialDirectory(Path.of(System.getProperty("user.home")).toFile())
        );

        if(allowedExtensions.contains("|")) {
            var extParts = allowedExtensions.split("\\|");
            for(var part : extParts) {
                fileChooser.getExtensionFilters().add(
                        new FileChooser.ExtensionFilter(part, part));
            }
        } else {
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter(allowedExtensions, allowedExtensions));
        }

        File f;
        if (open) {
            f = fileChooser.showOpenDialog(mainStage);
        } else {
            suggestedName.ifPresent(fileChooser::setInitialFileName);
            f = fileChooser.showSaveDialog(mainStage);
        }

        if (f != null) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

}
