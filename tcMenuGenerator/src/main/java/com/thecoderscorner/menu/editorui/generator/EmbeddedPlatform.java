package com.thecoderscorner.menu.editorui.generator;

public enum EmbeddedPlatform {
    ARDUINO_8BIT("Arduino - Uno, Mega, 8bit");

    private final String friendlyName;

    EmbeddedPlatform(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
