package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules.VAR_PATTERN;
import static com.thecoderscorner.menu.editorui.uimodel.UIMenuItem.NO_FUNCTION_DEFINED;

public class EditDialogController {
    public static final String CREATED_ON_YOUR_BEHALF = "The code for this is created for you by designer";
    public TextArea codeOutputArea;
    public ComboBox<String> callbackTypeCombo;
    public TextField functionNameField;
    private Optional<String> result;

    public void initialise(String fnDefinition, boolean runtimeItem) {
        if(runtimeItem) {
            callbackTypeCombo.setItems(FXCollections.observableArrayList(
                    "No Callback Defined",
                    "Function callback with implementation",
                    "Function callback definition only",
                    "Runtime RenderFn Override implementation",
                    "Runtime RenderFn Override definition only"
            ));
        } else {
            callbackTypeCombo.setItems(FXCollections.observableArrayList(
                    "No Callback Defined",
                    "Function callback with implementation",
                    "Function callback definition only"
            ));
        }

        if(StringHelper.isStringEmptyOrNull(fnDefinition) || fnDefinition.equals(NO_FUNCTION_DEFINED)) {
            callbackTypeCombo.getSelectionModel().select(0);
            functionNameField.setText("");
        } else if(fnDefinition.endsWith("Cb")) {
            if(fnDefinition.startsWith("@")) {
                callbackTypeCombo.getSelectionModel().select(3);
                if(fnDefinition.length() > 1) {
                    functionNameField.setText(fnDefinition.substring(1));
                }
            } else{
                callbackTypeCombo.getSelectionModel().select(1);
                functionNameField.setText(fnDefinition);
            }
        } else if(fnDefinition.startsWith("@")) {
            callbackTypeCombo.getSelectionModel().select(2);
            if(fnDefinition.length() > 1) {
                functionNameField.setText(fnDefinition.substring(1));
            }
        }
        onCallbackTypeChanged(null);
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        result = Optional.empty();
        ((Stage)callbackTypeCombo.getScene().getWindow()).close();
    }

    public void onUpdatePressed(ActionEvent actionEvent) {
        if(VAR_PATTERN.matcher(functionNameField.getText()).matches()) {
            switch (callbackTypeCombo.getSelectionModel().getSelectedIndex()) {
                case 0 -> Optional.of(NO_FUNCTION_DEFINED);
                case 1 -> Optional.of(functionNameField.getText());
                case 2 -> Optional.of("@" + functionNameField.getText());
                case 3 -> Optional.of(functionNameField.getText() + "RtCall");
                case 4 -> Optional.of("@" + functionNameField.getText() + "RtCall");
            }

        } else {
            result = Optional.empty();
            var alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Variable name invalid");
            alert.setHeaderText("Please check the variable name");
            alert.setContentText("Ensure there are no spaces in the variable name and that it only contains latin characters");
            BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
            alert.showAndWait();
        }
    }

    public void onCallbackTypeChanged(ActionEvent actionEvent) {
        functionNameField.setDisable(callbackTypeCombo.getSelectionModel().getSelectedIndex() == 0);
        codeOutputArea.setText(switch (callbackTypeCombo.getSelectionModel().getSelectedIndex()) {
            default -> CREATED_ON_YOUR_BEHALF;
            case 2 -> String.format("void CALLBACK_FUNCTION %s(int id) {", functionNameField.getText().replace("@", "")) + CoreCodeGenerator.LINE_BREAK + "}";
            case 4 -> String.format("int CALLBACK_FUNCTION %s", functionNameField.getText().replace("@", "")) +
                    CallbackRequirement.RUNTIME_CALLBACK_PARAMS + " {" + CoreCodeGenerator.LINE_BREAK + "}";
        });
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
    }

    public Optional<String> getResult() {
        return result;
    }
}
