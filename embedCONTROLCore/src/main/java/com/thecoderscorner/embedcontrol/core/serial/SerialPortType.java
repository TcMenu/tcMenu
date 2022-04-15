package com.thecoderscorner.embedcontrol.core.serial;

/**
 * Indicates the type of serial port
 */
public enum SerialPortType {
    /** A Regular USB serial port */
    REGULAR_USB_SERIAL,
    /** A bluetooth serial port */
    BLUETOOTH_SERIAL,
    /** A BLE serial port */
    BLE_SERIAL,
    /** Special enumeration only for searching that represents all possible ports */
    ALL_PORTS
}
