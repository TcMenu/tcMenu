package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.MenuEditorApp;
import com.thecoderscorner.menu.editorui.dialog.BaseDialogSupport;
import com.thecoderscorner.menu.editorui.uimodel.UrlsForDocumentation;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import com.thecoderscorner.menu.editorui.util.StringHelper;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;
import java.util.ResourceBundle;

import static com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement.*;
import static com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.validation.StringPropertyValidationRules.VAR_PATTERN;
import static com.thecoderscorner.menu.editorui.uimodel.UIMenuItem.NO_FUNCTION_DEFINED;

public class EditFunctionController {
    public TextArea codeOutputArea;
    public ComboBox<String> callbackTypeCombo;
    public TextField functionNameField;
    private Optional<String> result = Optional.empty();
    private MenuItem menuItem;
    private final ResourceBundle bundle = MenuEditorApp.getBundle();

    public void initialise(String fnDefinition, com.thecoderscorner.menu.domain.MenuItem menuItem) {
        this.menuItem = menuItem;
        boolean runtimeItem = isApplicableForOverrideRtCall(menuItem);
        if(runtimeItem) {
            callbackTypeCombo.setItems(FXCollections.observableArrayList(
                    bundle.getString("edit.function.no.callback"),
                    bundle.getString("edit.function.regular.callback"),
                    bundle.getString("edit.function.regular.callback.def"),
                    bundle.getString("edit.function.rtcall.callback"),
                    bundle.getString("edit.function.rtcall.callback.def")
            ));
        } else {
            callbackTypeCombo.setItems(FXCollections.observableArrayList(
                    bundle.getString("edit.function.no.callback"),
                    bundle.getString("edit.function.regular.callback"),
                    bundle.getString("edit.function.regular.callback.def")
            ));
        }

        functionNameField.textProperty().addListener((observableValue, s, t1) -> {
            onCallbackTypeChanged(null);
        });


        if(StringHelper.isStringEmptyOrNull(fnDefinition) || fnDefinition.equals(NO_FUNCTION_DEFINED)) {
            callbackTypeCombo.getSelectionModel().select(0);
            functionNameField.setText("");
        } else if(runtimeItem && fnDefinition.endsWith(RUNTIME_FUNCTION_SUFIX)) {
            fnDefinition = fnDefinition.replace(RUNTIME_FUNCTION_SUFIX, "");
            if(fnDefinition.startsWith("@")) {
                callbackTypeCombo.getSelectionModel().select(4);
                if(fnDefinition.length() > 1) {
                    functionNameField.setText(fnDefinition.substring(1));
                }
            } else{
                callbackTypeCombo.getSelectionModel().select(3);
                functionNameField.setText(fnDefinition);
            }
        } else if(fnDefinition.startsWith("@")) {
            callbackTypeCombo.getSelectionModel().select(2);
            if(fnDefinition.length() > 1) {
                functionNameField.setText(fnDefinition.substring(1));
            }
        } else {
            callbackTypeCombo.getSelectionModel().select(1);
            functionNameField.setText(fnDefinition);
        }
        onCallbackTypeChanged(null);
    }

    public void onCancelPressed(ActionEvent actionEvent) {
        result = Optional.empty();
        ((Stage)callbackTypeCombo.getScene().getWindow()).close();
    }

    public void onUpdatePressed(ActionEvent actionEvent) {
        int index = callbackTypeCombo.getSelectionModel().getSelectedIndex();
        if(index == 0) {
            result = Optional.empty();
            ((Stage)callbackTypeCombo.getScene().getWindow()).close();
        }
        else {
            String fnText = functionNameField.getText();
            if(VAR_PATTERN.matcher(fnText).matches() && fnText.length() > 0) {
                result = switch (index) {
                    default -> Optional.of(NO_FUNCTION_DEFINED);
                    case 1 -> Optional.of(fnText);
                    case 2 -> Optional.of("@" + fnText);
                    case 3 -> Optional.of(fnText + RUNTIME_FUNCTION_SUFIX);
                    case 4 -> Optional.of("@" + fnText + RUNTIME_FUNCTION_SUFIX);
                };
                ((Stage)callbackTypeCombo.getScene().getWindow()).close();
            } else {
                result = Optional.empty();
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(bundle.getString("edit.function.variable.error.title"));
                alert.setHeaderText(bundle.getString("edit.function.variable.error.header"));
                alert.setContentText(bundle.getString("edit.function.variable.error.message"));
                BaseDialogSupport.getJMetro().setScene(alert.getDialogPane().getScene());
                alert.showAndWait();
            }
        }
    }

    public void onCallbackTypeChanged(ActionEvent actionEvent) {
        int index = callbackTypeCombo.getSelectionModel().getSelectedIndex();
        functionNameField.setDisable(index == 0);
        final String variableName = functionNameField.getText().replace("@", "") + ((index > 2) ? RUNTIME_FUNCTION_SUFIX : "");
        codeOutputArea.setText(switch (index) {
            default -> bundle.getString("edit.function.code.created.for.you");
            case 2 -> String.format("void CALLBACK_FUNCTION %s(int id) {", variableName) + LINE_BREAK + "}";
            case 4 -> generateRtCallForType(menuItem, variableName, LINE_BREAK);
        });
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(UrlsForDocumentation.CORE_CALLBACK_MENU_DOCS_URL);
    }

    public Optional<String> getResult() {
        return result;
    }

}
