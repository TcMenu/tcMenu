/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PluginEmbeddedPlatformsImplTest {

    @Test
    void testEmbeddedPlatforms() {
        PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();
        assertThat(platforms.getEmbeddedPlatforms()).containsExactly(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32);

        assertEquals(ARDUINO_AVR, platforms.getEmbeddedPlatformFromId(ARDUINO_AVR.getBoardId()));
        assertEquals(ARDUINO32, platforms.getEmbeddedPlatformFromId(ARDUINO32.getBoardId()));
        assertEquals(ARDUINO_ESP8266, platforms.getEmbeddedPlatformFromId(ARDUINO_ESP8266.getBoardId()));
        assertEquals(ARDUINO_ESP32, platforms.getEmbeddedPlatformFromId(ARDUINO_ESP32.getBoardId()));
        assertThrows(IllegalArgumentException.class, () -> platforms.getEmbeddedPlatformFromId("invalidId"));

        CodeGeneratorOptions standardOptions = new CodeGeneratorOptions();
        var generator = platforms.getCodeGeneratorFor(ARDUINO_AVR, standardOptions);
        assertThat(generator).isOfAnyClassIn(ArduinoGenerator.class);

        assertThrows(IllegalArgumentException.class, () -> {
            EmbeddedPlatform invalidPlatform = new EmbeddedPlatform("", "Invalid", false);
            platforms.getCodeGeneratorFor(invalidPlatform, standardOptions);
        });
    }
}