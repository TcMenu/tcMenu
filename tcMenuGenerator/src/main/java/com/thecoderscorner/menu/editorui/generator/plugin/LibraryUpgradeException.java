package com.thecoderscorner.menu.editorui.generator.plugin;

public class LibraryUpgradeException extends Exception {

    public LibraryUpgradeException(String message) {
        super(message);
    }

    public LibraryUpgradeException(String detail, Throwable e) {
        super(detail, e);
    }
}
