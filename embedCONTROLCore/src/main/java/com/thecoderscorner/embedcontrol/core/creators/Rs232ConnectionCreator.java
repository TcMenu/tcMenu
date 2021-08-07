package com.thecoderscorner.embedcontrol.core.creators;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.thecoderscorner.embedcontrol.core.serial.PlatformSerialFactory;
import com.thecoderscorner.menu.remote.AuthStatus;
import com.thecoderscorner.menu.remote.RemoteMenuController;

import java.io.IOException;
import java.util.function.Consumer;

import static com.thecoderscorner.menu.persist.JsonMenuItemSerializer.*;

public class Rs232ConnectionCreator implements ConnectionCreator {
    public static final String MANUAL_RS232_CREATOR_TYPE = "rs232";
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

    public String getPortId() {
        return portId;
    }

    public int getBaudRate() {
        return baudRate;
    }

    @Override
    public AuthStatus currentState() {
        return controller != null ? controller.getConnector().getAuthenticationStatus() : AuthStatus.NOT_STARTED;
    }

    @Override
    public RemoteMenuController start() throws Exception {
        controller = serialFactory.getPortByIdWithBaud(portId, baudRate).orElseThrow();
        controller.start();
        return controller;
    }

    @Override
    public boolean attemptPairing(Consumer<AuthStatus> statusConsumer) throws IOException {
        return serialFactory.attemptPairing(name, baudRate, statusConsumer);
    }

    @Override
    public void load(JsonObject prefs) throws IOException {
        JsonObject creatorType = getJsonObjOrThrow(prefs, "creator");
        name = getJsonStrOrThrow(creatorType, "name");
        portId = getJsonStrOrThrow(creatorType, "portId");
        baudRate = getJsonIntOrThrow(creatorType, "baud");
    }

    @Override
    public void save(JsonObject prefs) {
        JsonObject creator = new JsonObject();
        creator.add("name", new JsonPrimitive(name));
        creator.add("portId", new JsonPrimitive(portId));
        creator.add("baud", new JsonPrimitive(baudRate));
        creator.add("type", new JsonPrimitive(MANUAL_RS232_CREATOR_TYPE));
        prefs.add("creator", creator);
    }
}
