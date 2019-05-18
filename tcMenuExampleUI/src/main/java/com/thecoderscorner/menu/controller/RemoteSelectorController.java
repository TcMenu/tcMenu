/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.controller;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;
import com.thecoderscorner.menu.remote.socket.SocketControllerBuilder;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

/**
 * At start up this class asks the user to define the tcMenu that they wish to connect to.
 * It does so by displaying a simple dialog. To make things less annoying, this dialog
 * tries to remember the last settings used.
 */
public class RemoteSelectorController {
    public static final String LOCAL_NAME = "JavaUI";
    public static final String PREF_KEY_CONNECTOR_TYPE = "ConnectorType";
    public static final String PREF_KEY_SERIAL_PORT = "serialPort";
    public static final String PREF_KEY_SERIAL_BAUD = "SerialBaud";
    public static final String PREF_KEY_IP_ADDRESS = "IpAddress";
    public static final String PREF_KEY_IP_PORT = "IpPort";
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
    private MenuTree menuTree;
    private RemoteMenuController result = null;

    public void init(MenuTree tree) {
        this.menuTree = tree;
        portCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).collect(Collectors.toList()))
        );
        Preferences prefs = Preferences.userNodeForPackage(RemoteSelectorController.class);
        var connector = prefs.get(PREF_KEY_CONNECTOR_TYPE, "serial");
        if(connector.equals("serial")) {
            chooseNetwork.setSelected(false);
            chooseSerial.setSelected(true);
            var port = prefs.get(PREF_KEY_SERIAL_PORT, "");
            var baud = prefs.get(PREF_KEY_SERIAL_BAUD, "115200");
            if(!port.isEmpty()) portCombo.getSelectionModel().select(port);
            baudText.setText(baud);
        }
        else {
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

        if(chooseSerial.isSelected()) {
            int baud = Integer.parseInt(baudText.getText());
            String port = portCombo.getSelectionModel().getSelectedItem();
            result = new Rs232ControllerBuilder()
                    .withLocalName(LOCAL_NAME)
                    .withMenuTree(menuTree)
                    .withRs232(port, baud)
                    .build();

        }
        else {
            int port = Integer.parseInt(ipPortText.getText());
            String ipAddr = addrText.getText();
            try {
                result = new SocketControllerBuilder()
                        .withAddress(ipAddr)
                        .withPort(port)
                        .withLocalName(LOCAL_NAME)
                        .withMenuTree(menuTree)
                        .build();
            } catch (IOException e) {
                throw new UnsupportedOperationException("Could not create socket");
            }
        }
        Stage s = (Stage) portLabel.getScene().getWindow();
        s.close();
    }

    private void saveLastSettings() {
        Preferences prefs = Preferences.userNodeForPackage(RemoteSelectorController.class);
        prefs.put(PREF_KEY_CONNECTOR_TYPE, chooseSerial.isSelected() ? "serial" : "ethernet");
        if(chooseSerial.isSelected()) {
            prefs.put(PREF_KEY_SERIAL_PORT, portCombo.getValue());
            prefs.put(PREF_KEY_SERIAL_BAUD, baudText.getText());
        }
        else {
            prefs.put(PREF_KEY_IP_ADDRESS, addrText.getText());
            prefs.put(PREF_KEY_IP_PORT, ipPortText.getText());
        }

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
