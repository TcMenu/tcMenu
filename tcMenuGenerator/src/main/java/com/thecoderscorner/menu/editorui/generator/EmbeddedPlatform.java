/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

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
