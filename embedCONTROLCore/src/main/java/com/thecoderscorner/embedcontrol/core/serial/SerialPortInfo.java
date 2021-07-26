package com.thecoderscorner.embedcontrol.core.serial;

import java.util.Objects;

public class SerialPortInfo {
    private final String printableName;
    private final String id;
    private final SerialPortType portType;
    private final double rssi;

    public SerialPortInfo(String printableName, String id, SerialPortType portType, double rssi) {
        this.printableName = printableName;
        this.id = id;
        this.portType = portType;
        this.rssi = rssi;
    }

    @Override
    public String toString() {
        var radioStrength = Double.isNaN(rssi) ? "" : String.format("%.2f", rssi);
        return nicePortType(portType) + " - " + printableName + radioStrength;
    }

    private String nicePortType(SerialPortType portType) {
        switch (portType) {
            case REGULAR_USB_SERIAL: return "Serial";
            case BLUETOOTH_SERIAL: return "Bluetooth";
            default: return "BLE";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerialPortInfo that = (SerialPortInfo) o;
        return Objects.equals(printableName, that.printableName) && Objects.equals(id, that.id) && portType == that.portType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(printableName, id, portType);
    }

    public String getPrintableName() {
        return printableName;
    }

    public String getId() {
        return id;
    }

    public SerialPortType getPortType() {
        return portType;
    }

    public double getRssi() {
        return rssi;
    }
}
