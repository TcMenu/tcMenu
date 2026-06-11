package com.thecoderscorner.embedcontrol.customization;

public class InvalidItemChangeException extends Exception {
    public InvalidItemChangeException(String message) {
        super(message);
    }

    public InvalidItemChangeException(String message, Throwable cause) {
        super(message, cause);
    }
}
