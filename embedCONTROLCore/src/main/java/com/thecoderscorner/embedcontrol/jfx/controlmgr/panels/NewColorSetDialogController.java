package com.thecoderscorner.embedcontrol.jfx.controlmgr.panels;

import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class NewColorSetDialogController {

    private Optional<String> result = Optional.empty();
    public TextField colorSetName;

    public void onCreateColorSet(ActionEvent actionEvent) {
        if(StringHelper.isStringEmptyOrNull(colorSetName.getText())) return;
        result = Optional.ofNullable(colorSetName.getText());
        ((Stage)(colorSetName.getScene().getWindow())).close();
    }

    public void onClose(ActionEvent actionEvent) {
        ((Stage)(colorSetName.getScene().getWindow())).close();
    }

    public Optional<String> getResult() {
        return result;
    }
}
