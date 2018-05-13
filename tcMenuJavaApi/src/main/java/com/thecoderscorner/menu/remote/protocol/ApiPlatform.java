/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.remote.protocol;

public enum ApiPlatform {
    ARDUINO_8(0, "Arduino 8bit"),
    JAVA_API(1, "Java API");


    private final int key;
    private final String description;

    ApiPlatform(int key, String description) {
        this.key = key;
        this.description = description;
    }

    public int getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }
}
