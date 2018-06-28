/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.examples.simpleui;

import com.fazecast.jSerialComm.SerialPort;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.remote.RemoteMenuController;
import com.thecoderscorner.menu.remote.rs232.Rs232ControllerBuilder;
import com.thecoderscorner.menu.remote.udp.UdpControllerBuilder;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * This controller manages the choice of remote that will be used at startup. Kept as simple as possible.
 */
public class RemoteSelectorController {
    public static final String LOCAL_NAME = "JavaUI";
    public Label portLabel;
    public Label baudLabel;
    public TextField baudText;
    public ComboBox<String> portCombo;
    public RadioButton chooseSerial;
    public RadioButton chooseNetwork;
    public Label addrLabel;
    public Label devIdLabel;
    public TextField addrText;
    public TextField devIdText;
    public Label ipPortLabel;
    public TextField ipPortText;
    private MenuTree menuTree;
    private RemoteMenuController result = null;

    public void init(MenuTree tree) {
        this.menuTree = tree;
        portCombo.setItems(FXCollections.observableArrayList(
                Arrays.stream(SerialPort.getCommPorts()).map(SerialPort::getSystemPortName).collect(Collectors.toList()))
        );
    }

    public void onCommChoiceChange(ActionEvent actionEvent) {
        boolean enableSerial = chooseSerial.isSelected();

        portLabel.setDisable(!enableSerial);
        baudLabel.setDisable(!enableSerial);
        baudText.setDisable(!enableSerial);
        portCombo.setDisable(!enableSerial);

        addrLabel.setDisable(enableSerial);
        devIdLabel.setDisable(enableSerial);
        addrText.setDisable(enableSerial);
        devIdText.setDisable(enableSerial);
        ipPortLabel.setDisable(enableSerial);
        ipPortText.setDisable(enableSerial);
    }

    public void onCancel(ActionEvent actionEvent) {
        Stage s = (Stage) portLabel.getScene().getWindow();
        s.close();
    }

    public void onStart(ActionEvent actionEvent) {
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
            short devId = (short) Integer.parseInt(devIdText.getText());
            String address = addrText.getText();
            int port = Integer.parseInt(ipPortText.getText());
            result = new UdpControllerBuilder()
                    .withLocalName(LOCAL_NAME)
                    .withMenuTree(menuTree)
                    .withBindAddress(new InetSocketAddress(address, port))
                    .withDeviceId(devId)
                    .build();
        }
        Stage s = (Stage) portLabel.getScene().getWindow();
        s.close();
    }

    public RemoteMenuController getResult() {
        return result;
    }
}
