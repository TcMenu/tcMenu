package com.thecoderscorner.menu.domain.util;

public class MenuItemFormatException extends Exception{
    public MenuItemFormatException(String message) {
        super(message);
    }

    public MenuItemFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}
