/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.ConnectorFactory;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.protocol.PairingHelper;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * At start up this class asks the user to define the tcMenu that they wish to connect to.
 * It does so by displaying a simple dialog. To make things less annoying, this dialog
 * tries to remember the last settings used.
 */
public class RemoteSelectorController {
    public static final String LOCAL_NAME = "menuControl";
    public static final String PREF_KEY_CONNECTOR_TYPE = "ConnectorType";
    public static final String PREF_KEY_SERIAL_PORT = "serialPort";
    public static final String PREF_KEY_SERIAL_BAUD = "SerialBaud";
    public static final String PREF_KEY_IP_ADDRESS = "IpAddress";
    public static final String PREF_KEY_IP_PORT = "IpPort";
    private static final String PREF_KEY_NAME = "MyName";
    private static final String PREF_KEY_UUID = "MyUUID";
    private static final String HELP_URL = "https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-remote-connection-arduino-desktop/";

    public Label portLabel;
    public Label baudLabel;
    public TextField baudText;
    public ComboBox<String> portCombo;
    public RadioButton chooseSerial;
    public RadioButton chooseNetwork;
    public Label addrLabel;
    public TextField addrText;
    public Label ipPortLabel;
    public TextField ipPortText;
    public TextField myUUID;
    public TextField myName;
    private MenuTree menuTree;
    private RemoteMenuController result = null;

    Popup pairingPopup = new Popup();
    private VBox pairingPane;

    public void init(MenuTree tree) {
        this.menuTree = tree;
        portCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).collect(Collectors.toList()))
        );
        Preferences prefs = Preferences.userNodeForPackage(RemoteSelectorController.class);
        var name = prefs.get(PREF_KEY_NAME, "my-connector");
        var uuid = prefs.get(PREF_KEY_UUID, "");
        if(uuid.isEmpty()) {
            uuid = UUID.randomUUID().toString();
            prefs.put(PREF_KEY_UUID, uuid);
        }
        myName.setText(name);
        myUUID.setText(uuid);

        var connector = prefs.get(PREF_KEY_CONNECTOR_TYPE, "serial");
        if (connector.equals("serial")) {
            chooseNetwork.setSelected(false);
            chooseSerial.setSelected(true);
            var port = prefs.get(PREF_KEY_SERIAL_PORT, "");
            var baud = prefs.get(PREF_KEY_SERIAL_BAUD, "115200");
            if (!port.isEmpty()) portCombo.getSelectionModel().select(port);
            baudText.setText(baud);
        } else {
            chooseSerial.setSelected(false);
            chooseNetwork.setSelected(true);
            var eth = prefs.get(PREF_KEY_IP_ADDRESS, "192.168.0.96");
            var port = prefs.get(PREF_KEY_IP_PORT, "3333");
            addrText.setText(eth);
            ipPortText.setText(port);
        }
        onCommChoiceChange(new ActionEvent());
    }

    public void onCommChoiceChange(ActionEvent actionEvent) {
        boolean enableSerial = chooseSerial.isSelected();

        portLabel.setDisable(!enableSerial);
        baudLabel.setDisable(!enableSerial);
        baudText.setDisable(!enableSerial);
        portCombo.setDisable(!enableSerial);

        addrLabel.setDisable(enableSerial);
        addrText.setDisable(enableSerial);
        ipPortLabel.setDisable(enableSerial);
        ipPortText.setDisable(enableSerial);
    }

    public void onCancel(ActionEvent actionEvent) {
        Stage s = (Stage) portLabel.getScene().getWindow();
        s.close();
    }

    public void onStart(ActionEvent actionEvent) {

        saveLastSettings();

        result = createBuilder().build();
        Stage s = (Stage) portLabel.getScene().getWindow();
        s.close();
    }

    private ConnectorFactory createBuilder() {
        if (chooseSerial.isSelected()) {
            int baud = Integer.parseInt(baudText.getText());
            String port = portCombo.getSelectionModel().getSelectedItem();
            return new Rs232ControllerBuilder()
                    .withLocalName(myName.getText())
                    .withMenuTree(menuTree)
                    .withRs232(port, baud)
                    .withUUID(UUID.fromString(myUUID.getText()));

        } else {
            int port = Integer.parseInt(ipPortText.getText());
            String ipAddr = addrText.getText();
            return new SocketControllerBuilder()
                    .withAddress(ipAddr)
                    .withPort(port)
                    .withLocalName(myName.getText())
                    .withUUID(UUID.fromString(myUUID.getText()))
                    .withMenuTree(menuTree);
        }
    }

    public void onPairRequest(ActionEvent actionEvent) {
        saveLastSettings();

        Label labelHeading = new Label("Pairing mode");
        labelHeading.setStyle("-fx-font-size: 125%; -fx-font-weight: bold;");
        Label labelHelp = new Label("Look on the device screen, there should be a pairing request on it, press ACCEPT to allow it.");
        labelHelp.setWrapText(true);
        Label labelPairStatus = new Label("Starting pairing process");

        pairingPane = new VBox(6.0);
        pairingPane.setPrefSize(400, 150);
        pairingPane.setPadding(new Insets(16.0));
        pairingPane.setBackground(new Background(new BackgroundFill(Color.color(1.0, 1.0, 1.0),
                new CornerRadii(3.0), Insets.EMPTY)));
        pairingPane.setBorder(new Border(new BorderStroke(Color.color(0., 0., 0.), BorderStrokeStyle.SOLID,
                new CornerRadii(3.0), BorderStroke.DEFAULT_WIDTHS)));
        pairingPane.getChildren().add(labelHeading);
        pairingPane.getChildren().add(labelHelp);
        pairingPane.getChildren().add(labelPairStatus);
        pairingPane.getStyleClass().add("popupWindow");
        pairingPopup.getContent().add(pairingPane);
        pairingPopup.setAutoHide(false);
        pairingPopup.setHideOnEscape(false);
        pairingPopup.show(portCombo.getScene().getWindow());

        // this receives updates during the pairing process and displays them in the label.
        Optional<Consumer<PairingHelper.PairingState>> stateConsumer = Optional.of((sts) -> {
            Platform.runLater(() -> labelPairStatus.setText("Pairing status: " + sts.toString()));
        });

        new Thread(()-> {
            pairCompleted(createBuilder().attemptPairing(stateConsumer));
        }).start();
    }

    private void pairCompleted(boolean success) {
        Button closeButton = new Button(success ? "Successful - Close" : "Unsuccessful - Close");
        closeButton.setOnAction((e)-> {
            pairingPane.getChildren().clear();
            pairingPopup.hide();
        });
        Platform.runLater(()-> pairingPane.getChildren().add(closeButton));
    }


    private void saveLastSettings() {
        Preferences prefs = Preferences.userNodeForPackage(RemoteSelectorController.class);
        prefs.put(PREF_KEY_CONNECTOR_TYPE, chooseSerial.isSelected() ? "serial" : "ethernet");
        prefs.put(PREF_KEY_CONNECTOR_TYPE, chooseSerial.isSelected() ? "serial" : "ethernet");
        prefs.put(PREF_KEY_NAME, myName.getText());
        if(portCombo.getValue() != null) prefs.put(PREF_KEY_SERIAL_PORT, portCombo.getValue());
        prefs.put(PREF_KEY_SERIAL_BAUD, baudText.getText());
        prefs.put(PREF_KEY_IP_ADDRESS, addrText.getText());
        prefs.put(PREF_KEY_IP_PORT, ipPortText.getText());
    }

    public RemoteMenuController getResult() {
        return result;
    }

    public void onClickHelp(MouseEvent mouseEvent) {
        try {
            Desktop.getDesktop().browse(new URI(HELP_URL));
        } catch (IOException | URISyntaxException e) {
            // not much we can do here really!
        }

    }
}
