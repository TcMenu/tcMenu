package com.thecoderscorner.menu.editorui.controller;

import com.thecoderscorner.menu.editorui.dialog.AppInformationPanel;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.expander.*;
import com.thecoderscorner.menu.editorui.util.SafeNavigator;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Collection;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;

public class ConfigureExpanderController {
    private final System.Logger logger = System.getLogger(ConfigureExpanderController.class.getSimpleName());
    public final static String[] COMBO_CHOICES = { "Custom IoAbstractionRef", "I2C PCF8574", "I2C MCP23017", "I2C PCF8575", "I2C AW9523" };

    public ComboBox<String> expanderTypeCombo;
    public TextField variableNameField;
    public TextField i2cAddrField;
    public TextField interruptPinField;
    public Button setExpanderButton;
    public Label helpTextField;
    public CheckBox invertedField;

    private boolean newDialog;
    private Collection<String> namesInUse;
    private Optional<IoExpanderDefinition> result = Optional.empty();

    public void initialise(IoExpanderDefinition expanderDefinition, Collection<String> namesInUse) {
        this.namesInUse = namesInUse;
        newDialog = expanderDefinition == null;
        expanderTypeCombo.getItems().addAll(COMBO_CHOICES);

        expanderTypeCombo.getSelectionModel().selectedIndexProperty().addListener((ov, number, t1) -> reEvaluateForm());
        variableNameField.textProperty().addListener((ov, s, t1) -> reEvaluateForm());
        i2cAddrField.textProperty().addListener((ov, s, t1) -> reEvaluateForm());
        interruptPinField.textProperty().addListener((ov, s, t1) -> reEvaluateForm());

        if(expanderDefinition != null) {
            variableNameField.setText(expanderDefinition.getId());
            if (expanderDefinition instanceof CustomDeviceExpander) {
                expanderTypeCombo.getSelectionModel().select(0);
            } else if (expanderDefinition instanceof Pcf8574DeviceExpander pcf) {
                expanderTypeCombo.getSelectionModel().select(1);
                i2cAddrField.setText("0x" + Integer.toString(pcf.getI2cAddress(), 16));
                interruptPinField.setText(Integer.toString(pcf.getIntPin()));
            } else if (expanderDefinition instanceof Pcf8575DeviceExpander pcf) {
                expanderTypeCombo.getSelectionModel().select(3);
                i2cAddrField.setText("0x" + Integer.toString(pcf.getI2cAddress(), 16));
                interruptPinField.setText(Integer.toString(pcf.getIntPin()));
            } else if (expanderDefinition instanceof Mcp23017DeviceExpander pcf) {
                expanderTypeCombo.getSelectionModel().select(2);
                i2cAddrField.setText("0x" + Integer.toString(pcf.getI2cAddress(), 16));
                interruptPinField.setText(Integer.toString(pcf.getIntPin()));
            } else if(expanderDefinition instanceof Aw9523DeviceExpander aw) {
                expanderTypeCombo.getSelectionModel().select(4);
                i2cAddrField.setText("0x" + Integer.toString(aw.getI2cAddress(), 16));
                interruptPinField.setText(Integer.toString(aw.getIntPin()));
            }
        }
        else {
            expanderTypeCombo.getSelectionModel().select(0);
        }

        reEvaluateForm();
    }

    private void reEvaluateForm() {
        int idx = expanderTypeCombo.getSelectionModel().getSelectedIndex();
        if (idx == 0) {// custom IO
            i2cAddrField.setDisable(true);
            interruptPinField.setDisable(true);
            variableNameField.setDisable(!newDialog);
            setExpanderButton.setDisable(!anyEmptyText(variableNameField));
        } else {
            // i2c expanders
            i2cAddrField.setDisable(false);
            interruptPinField.setDisable(false);
            variableNameField.setDisable(!newDialog);
            setExpanderButton.setDisable(!anyEmptyText(variableNameField, i2cAddrField, interruptPinField));
        }
        invertedField.setDisable(idx != 1 && idx != 3); // only supported on PCF devices.
        helpTextField.setText(getDescriptiveTextForType());
        i2cAddrField.setPromptText((idx == 4) ? "0x58" : "0x20");
    }

    private boolean anyEmptyText(TextField... flds) {
        for(var f : flds) {
            if(f.getText().isEmpty()) return false;
        }
        return true;
    }


    public Optional<IoExpanderDefinition> getResult() {
        return result;
    }

    public void onCancel(ActionEvent actionEvent) {
        ((Stage)interruptPinField.getScene().getWindow()).close();
    }

    public void onSetExpander(ActionEvent actionEvent) {
        if(newDialog && namesInUse.contains(variableNameField.getText())) {
            var alert = new Alert(Alert.AlertType.ERROR, "The name you have chosen is already in use");
            alert.showAndWait();
            return;
        }
        try {
            result = switch (expanderTypeCombo.getSelectionModel().getSelectedIndex()) {
                case 0 -> Optional.of(new CustomDeviceExpander(variableNameField.getText()));
                case 1 -> Optional.of(new Pcf8574DeviceExpander(variableNameField.getText(), fromHex(i2cAddrField.getText()), Integer.parseInt(interruptPinField.getText()), invertedField.isSelected()));
                case 2 -> Optional.of(new Mcp23017DeviceExpander(variableNameField.getText(), fromHex(i2cAddrField.getText()), Integer.parseInt(interruptPinField.getText())));
                case 3 -> Optional.of(new Pcf8575DeviceExpander(variableNameField.getText(), fromHex(i2cAddrField.getText()), Integer.parseInt(interruptPinField.getText()), invertedField.isSelected()));
                case 4 -> Optional.of(new Aw9523DeviceExpander(variableNameField.getText(), fromHex(i2cAddrField.getText()), Integer.parseInt(interruptPinField.getText())));
                default -> Optional.empty();
            };
            ((Stage) interruptPinField.getScene().getWindow()).close();
        } catch (Exception ex) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Please ensure field values such as address and interrupt pin are valid", ButtonType.CLOSE);
            alert.showAndWait();
            logger.log(ERROR, "Field validation error in configure expander", ex);
        }

    }

    private int fromHex(String text) {
        if(text.startsWith("0x")) {
            text = text.substring(2);
            return Integer.parseInt(text, 16);
        }
        else return Integer.parseInt(text);
    }

    public void onOnlineHelp(ActionEvent actionEvent) {
        SafeNavigator.safeNavigateTo(AppInformationPanel.IO_EXPANDER_GUIDE_PAGE);
    }

    private String getDescriptiveTextForType() {
        return switch (expanderTypeCombo.getSelectionModel().getSelectedIndex()) {
            case 0 -> "You create an IoAbstractionRef in your sketch, take down the variable name, and reference that variable here";
            case 1 -> "PCF8574 8-bit I2C based IO expander created by designer for you to use. You must call Wire.begin() during setup.";
            case 2 -> "MCP23017 16-bit I2C based IO expander created by designer for you to use. You must call Wire.begin() during setup.";
            case 3 -> "PCF8575 16-bit I2C based IO expander created by designer for you to use. You must call Wire.begin() during setup.";
            default -> "Unknown option";
        };
    }
}
