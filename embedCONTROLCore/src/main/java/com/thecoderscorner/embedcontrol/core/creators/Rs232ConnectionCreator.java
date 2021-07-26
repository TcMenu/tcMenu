package com.thecoderscorner.embedcontrol.core.creators;

import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.util.prefs.Preferences;

public class Rs232ConnectionCreator implements ConnectionCreator {
    private final PlatformSerialFactory serialFactory;
    private String name;
    private String portId;
    private int baudRate;
    private RemoteMenuController controller;

    public Rs232ConnectionCreator(PlatformSerialFactory serialFactory) {
        this.serialFactory = serialFactory;
        name = portId = "";
        baudRate = 0;
    }

    public Rs232ConnectionCreator(PlatformSerialFactory serialFactory, String name, String portId, int baudRate) {
        this.serialFactory = serialFactory;
        this.name = name;
        this.portId = portId;
        this.baudRate = baudRate;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AuthStatus currentState() {
        return controller != null ? controller.getConnector().getAuthenticationStatus() : AuthStatus.NOT_STARTED;
    }

    @Override
    public RemoteMenuController start() throws Exception {
        controller = serialFactory.getPortByIdWithBaud(name, baudRate).orElseThrow();
        return controller;
    }

    @Override
    public void load(Preferences prefs) {
        name = prefs.get("name", "?");
        portId = prefs.get("portId", "?");
        baudRate = prefs.getInt("baud", 9600);
    }

    @Override
    public void save(Preferences prefs) {
        prefs.put("name", name);
        prefs.put("portId", portId);
        prefs.putInt("baudRate", baudRate);
    }
}
