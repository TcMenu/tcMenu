package com.thecoderscorner.embedcontrol.jfx.dialog;

import com.thecoderscorner.embedcontrol.core.creators.ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.ManualLanConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.Rs232ConnectionCreator;
import com.thecoderscorner.embedcontrol.core.creators.SimulatorConnectionCreator;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortInfo;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortType;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    private PlatformSerialFactory serialFactory;
    private Consumer<ConnectionCreator> creatorConsumer;
    private Set<SerialPortInfo> allPorts = new HashSet<>();
    private ScheduledExecutorService executorService;
    private GlobalSettings settings;

    public void initialise(GlobalSettings settings, ScheduledExecutorService executorService,
                           PlatformSerialFactory serialFactory, Consumer<ConnectionCreator> creatorConsumer) {
        this.serialFactory = serialFactory;
        this.creatorConsumer = creatorConsumer;
        this.executorService = executorService;
        this.settings = settings;
        baudCombo.getItems().addAll(BAUD_RATES);
        serialFactory.startPortScan(SerialPortType.ALL_PORTS, this::portChange);
        baudCombo.getSelectionModel().select(0);
        onRadioChange(null);

        hostNameField.textProperty().addListener((ov, o, n) -> validateFields());
        connectionNameField.textProperty().addListener((ov, o, n) -> validateFields());
        portNumberField.textProperty().addListener((ov, o, n) -> validateFields());
        serialPortCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
        baudCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
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
        serialFactory.stopPortScan();
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
        if(createSerialRadio.isSelected()) {
            creatorConsumer.accept(new Rs232ConnectionCreator(serialFactory, connectionNameField.getText(),
                    serialPortCombo.getSelectionModel().getSelectedItem().getId(),
                    baudCombo.getSelectionModel().getSelectedItem()));
        }
        else if(simulatorRadio.isSelected()) {
            creatorConsumer.accept(new SimulatorConnectionCreator(jsonDataField.getText(), connectionNameField.getText(),
                    executorService));
        }
        else {
            var portNum = Integer.parseInt(portNumberField.getText());
            creatorConsumer.accept(new ManualLanConnectionCreator(settings, executorService,
                    connectionNameField.getText(), hostNameField.getText(), portNum));
        }
        connectionNameField.setText("");
        createSerialRadio.setSelected(true);
        hostNameField.setText("");
        portNumberField.setText("");
        jsonDataField.setText("");
        validateFields();
        onRadioChange(null);
    }
}
