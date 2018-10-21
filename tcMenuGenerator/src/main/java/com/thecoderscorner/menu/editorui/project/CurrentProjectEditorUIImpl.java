package com.thecoderscorner.menu.editorui.project;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class CurrentProjectEditorUIImpl implements CurrentProjectEditorUI {

    private Stage mainStage;

    public CurrentProjectEditorUIImpl(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @Override
    public Optional<String> findFileNameFromUser(boolean open) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose a Menu File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Embedded menu", "*.emf"));
        File f;
        if (open) {
            f = fileChooser.showOpenDialog(mainStage);
        } else {
            f = fileChooser.showSaveDialog(mainStage);
        }

        if (f != null) {
            return Optional.of(f.getPath());
        }
        return Optional.empty();
    }

    @Override
    public void alertOnError(String heading, String description) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(heading);
        alert.setHeaderText(heading);
        alert.setContentText(description);
        alert.showAndWait();
    }

    @Override
    public boolean questionYesNo(String title, String header) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    @Override
    public void setTitle(String s) {
        mainStage.setTitle(s);
    }
}
