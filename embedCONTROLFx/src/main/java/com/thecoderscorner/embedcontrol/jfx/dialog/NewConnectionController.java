package com.thecoderscorner.embedcontrol.jfx.dialog;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortInfo;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortType;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.jfx.EmbedControlContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NewConnectionController {
    public static final List<Integer> BAUD_RATES = List.of(1200, 2400, 4800, 9600, 14400, 19200, 38400, 57600, 115200, 128000, 256000);
    public RadioButton createSerialRadio;
    public RadioButton createLanRadio;
    public ComboBox<SerialPortInfo> serialPortCombo;
    public ComboBox<Integer> baudCombo;
    public TextField hostNameField;
    public TextField portNumberField;
    public Button createButton;
    public TextField connectionNameField;
    public TextArea jsonDataField;
    public RadioButton simulatorRadio;
    private final Set<SerialPortInfo> allPorts = new HashSet<>();
    private GlobalSettings settings;
    private EmbedControlContext context;
    private Optional<ConnectionCreator> result = Optional.empty();
    private boolean newConnectionPanel;

    public void initialise(GlobalSettings settings, EmbedControlContext context, Optional<ConnectionCreator> existingCreator) {
        this.settings = settings;
        this.context = context;
        baudCombo.getItems().addAll(BAUD_RATES);
        context.getSerialFactory().startPortScan(SerialPortType.ALL_PORTS, this::portChange);
        baudCombo.getSelectionModel().select(0);

        newConnectionPanel = true;
        existingCreator.ifPresent(this::fillInFromExisting);

        onRadioChange(null);
        hostNameField.textProperty().addListener((ov, o, n) -> validateFields());
        connectionNameField.textProperty().addListener((ov, o, n) -> validateFields());
        portNumberField.textProperty().addListener((ov, o, n) -> validateFields());
        serialPortCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
        baudCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
    }

    private void fillInFromExisting(ConnectionCreator connectionCreator) {
        newConnectionPanel = false;
        createButton.setText("Save Changes");
        connectionNameField.setText(connectionCreator.getName());
        if(connectionCreator instanceof SimulatorConnectionCreator simCreator) {
            simulatorRadio.setSelected(true);
            jsonDataField.setText(simCreator.getJsonForTree());
        }
        else if(connectionCreator instanceof Rs232ConnectionCreator serCreator) {
            createSerialRadio.setSelected(true);
            var thePort = allPorts.stream().filter(sp -> sp.getId().equals(serCreator.getPortId())).findFirst();
            thePort.ifPresent(serialPortInfo -> serialPortCombo.getSelectionModel().select(serialPortInfo));
            baudCombo.getSelectionModel().select((Integer) serCreator.getBaudRate());
        }
        else if(connectionCreator instanceof ManualLanConnectionCreator lanCreator) {
            createLanRadio.setSelected(true);
            hostNameField.setText(lanCreator.getIpAddr());
            portNumberField.setText(Integer.toString(lanCreator.getPort()));
        }
    }

    private void validateFields() {
        boolean nameOk = !StringHelper.isStringEmptyOrNull(connectionNameField.getText());
        if(createSerialRadio.isSelected()) {
            createButton.setDisable(!nameOk ||
                    baudCombo.getSelectionModel().getSelectedIndex() == -1 ||
                    serialPortCombo.getSelectionModel().getSelectedIndex() == -1);
        }
        else if(createLanRadio.isSelected()){
            createButton.setDisable(
                    !(nameOk && hostNameField.getText().matches("\\d+\\.\\d+\\.\\d+\\.\\d+") &&
                    portNumberField.getText().matches("\\d+"))
            );

        }
        else {
            createButton.setDisable(false);
        }
    }

    public void destroy() {
        context.getSerialFactory().stopPortScan();
    }

    private void portChange(SerialPortInfo serialPortInfo) {
        Platform.runLater(() -> {
            allPorts.add(serialPortInfo);
            serialPortCombo.setItems(FXCollections.observableList(allPorts.stream().toList()));
        });
    }

    public void onRadioChange(ActionEvent actionEvent) {
        baudCombo.setDisable(!createSerialRadio.isSelected());
        serialPortCombo.setDisable(!createSerialRadio.isSelected());
        hostNameField.setDisable(!createLanRadio.isSelected());
        portNumberField.setDisable(!createLanRadio.isSelected());
        jsonDataField.setDisable(!simulatorRadio.isSelected());
        validateFields();
    }

    public void onCreate(ActionEvent actionEvent) {
        ConnectionCreator creator;
        if(createSerialRadio.isSelected()) {
            creator = new Rs232ConnectionCreator(context.getSerialFactory(), connectionNameField.getText(),
                    serialPortCombo.getSelectionModel().getSelectedItem().getId(),
                    baudCombo.getSelectionModel().getSelectedItem());
        }
        else if(simulatorRadio.isSelected()) {
            creator = new SimulatorConnectionCreator(jsonDataField.getText(), connectionNameField.getText(),
                    context.getExecutorService(), context.getSerializer());
        }
        else {
            var portNum = Integer.parseInt(portNumberField.getText());
            creator = new ManualLanConnectionCreator(settings, context.getExecutorService(),
                    connectionNameField.getText(), hostNameField.getText(), portNum);
        }

        result = Optional.of(creator);

        if(newConnectionPanel) {
            context.createConnection(creator);
        }
        else {
            ((Stage)connectionNameField.getScene().getWindow()).close();
        }

        connectionNameField.setText("");
        createSerialRadio.setSelected(true);
        hostNameField.setText("");
        portNumberField.setText("");
        jsonDataField.setText("");
        validateFields();
        onRadioChange(null);
    }

    public Optional<ConnectionCreator> getResult() {
        return result;
    }
}
