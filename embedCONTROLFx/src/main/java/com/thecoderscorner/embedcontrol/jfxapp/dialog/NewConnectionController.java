package com.thecoderscorner.embedcontrol.jfxapp.dialog;

import com.thecoderscorner.embedcontrol.core.serial.SerialPortInfo;
import com.thecoderscorner.embedcontrol.core.serial.SerialPortType;
import com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.embedcontrol.jfx.controlmgr.JfxNavigationManager;
import com.thecoderscorner.embedcontrol.jfxapp.EmbedControlContext;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import static com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection.StoreConnectionType;
import static com.thecoderscorner.embedcontrol.core.service.TcMenuPersistedConnection.StoreConnectionType.*;

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
    private EmbedControlContext context;
    private Consumer<TcMenuPersistedConnection> onUpdate;
    private TcMenuPersistedConnection existingPersistence = null;
    private JfxNavigationManager navigationManager;

    public void initialise(JfxNavigationManager navigationManager, EmbedControlContext context,
                           Optional<TcMenuPersistedConnection> existingCreator, Consumer<TcMenuPersistedConnection> onUpdate) {
        this.navigationManager = navigationManager;
        this.context = context;
        this.onUpdate = onUpdate;
        baudCombo.getItems().addAll(BAUD_RATES);
        context.getSerialFactory().startPortScan(SerialPortType.ALL_PORTS, this::portChange);
        baudCombo.getSelectionModel().select(0);

        existingCreator.ifPresent(this::fillInFromExisting);

        onRadioChange(null);
        hostNameField.textProperty().addListener((ov, o, n) -> validateFields());
        connectionNameField.textProperty().addListener((ov, o, n) -> validateFields());
        portNumberField.textProperty().addListener((ov, o, n) -> validateFields());
        serialPortCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
        baudCombo.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> validateFields());
    }

    private void fillInFromExisting(TcMenuPersistedConnection existing) {
        existingPersistence = existing;
        createButton.setText("Save Changes");
        connectionNameField.setText(existing.getName());
        if(existing.getConnectionType() == SIMULATOR) {
            simulatorRadio.setSelected(true);
            jsonDataField.setText(existing.getExtraData());
        }
        else if(existing.getConnectionType() == SERIAL_CONNECTION) {
            createSerialRadio.setSelected(true);
            var thePort = allPorts.stream().filter(sp -> sp.getId().equals(existing.getHostOrSerialId())).findFirst();
            thePort.ifPresent(serialPortInfo -> serialPortCombo.getSelectionModel().select(serialPortInfo));
            baudCombo.getSelectionModel().select((Integer.parseInt(existing.getPortOrBaud())));
        }
        else if(existing.getConnectionType() == MANUAL_SOCKET) {
            createLanRadio.setSelected(true);
            hostNameField.setText(existing.getHostOrSerialId());
            portNumberField.setText(existing.getPortOrBaud());
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
            createButton.setDisable(!nameOk);
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
        StoreConnectionType ty;
        String hostOrPort;
        String baudOrPort;
        String extra;
        if(createSerialRadio.isSelected()) {
            ty = SERIAL_CONNECTION;
            hostOrPort = serialPortCombo.getSelectionModel().getSelectedItem().getId();
            baudOrPort = String.valueOf(baudCombo.getSelectionModel().getSelectedItem());
            extra = "";
        }
        else if(simulatorRadio.isSelected()) {
            ty = SIMULATOR;
            hostOrPort = "";
            baudOrPort = "";
            extra = jsonDataField.getText();
        }
        else {
            ty = MANUAL_SOCKET;
            hostOrPort = hostNameField.getText();
            baudOrPort = portNumberField.getText();
            extra = "";
        }

        if(existingPersistence != null && navigationManager != null && onUpdate != null) {
            navigationManager.popNavigation();
            onUpdate.accept(new TcMenuPersistedConnection(
                    existingPersistence.getLocalId(), connectionNameField.getText(), existingPersistence.getUuid(),
                    existingPersistence.getFormName(), ty, hostOrPort, baudOrPort, extra, context.getDataStore().getUtilities()));
        }
        else {
            context.createConnection(new TcMenuPersistedConnection(
                    -1, connectionNameField.getText(), "", "",
                    ty, hostOrPort, baudOrPort, extra, context.getDataStore().getUtilities()));
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
