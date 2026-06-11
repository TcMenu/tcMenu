package com.thecoderscorner.menu.domain.util;

/**
 * This exception indicates that a particular item could not be formatted
 */
public class MenuItemFormatException extends Exception{
    public MenuItemFormatException(String message) {
        super(message);
    }

    public MenuItemFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
