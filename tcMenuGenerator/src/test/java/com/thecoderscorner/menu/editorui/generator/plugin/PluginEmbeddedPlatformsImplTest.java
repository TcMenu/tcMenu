/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorSupplier;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class PluginEmbeddedPlatformsImplTest {

    @Test
    void testEmbeddedPlatforms() {
        PluginEmbeddedPlatformsImpl platforms = new PluginEmbeddedPlatformsImpl();
        var codeGenSupplier = new CodeGeneratorSupplier(mock(ConfigurationStorage.class), mock(ArduinoLibraryInstaller.class));
        assertThat(platforms.getEmbeddedPlatforms()).containsExactly(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, STM32DUINO, RASPBERRY_PIJ, MBED_RTOS);

        assertEquals(ARDUINO_AVR, platforms.getEmbeddedPlatformFromId(ARDUINO_AVR.getBoardId()));
        assertEquals(ARDUINO32, platforms.getEmbeddedPlatformFromId(ARDUINO32.getBoardId()));
        assertEquals(ARDUINO_ESP8266, platforms.getEmbeddedPlatformFromId(ARDUINO_ESP8266.getBoardId()));
        assertEquals(ARDUINO_ESP32, platforms.getEmbeddedPlatformFromId(ARDUINO_ESP32.getBoardId()));
        assertEquals(MBED_RTOS, platforms.getEmbeddedPlatformFromId(MBED_RTOS.getBoardId()));
        assertEquals(STM32DUINO, platforms.getEmbeddedPlatformFromId(STM32DUINO.getBoardId()));
        assertEquals(RASPBERRY_PIJ, platforms.getEmbeddedPlatformFromId(RASPBERRY_PIJ.getBoardId()));
        assertThrows(IllegalArgumentException.class, () -> platforms.getEmbeddedPlatformFromId("invalidId"));

        assertTrue(ARDUINO_AVR.isUsesProgmem());
        assertFalse(ARDUINO32.isUsesProgmem());
        assertTrue(ARDUINO_ESP32.isUsesProgmem());
        assertTrue(ARDUINO_ESP8266.isUsesProgmem());
        assertFalse(STM32DUINO.isUsesProgmem());
        assertFalse(MBED_RTOS.isUsesProgmem());
        assertFalse(RASPBERRY_PIJ.isUsesProgmem());

        assertTrue(platforms.isJava(RASPBERRY_PIJ));
        assertFalse(platforms.isJava(ARDUINO_ESP32));
        assertTrue(platforms.isArduino(ARDUINO_ESP32));
        assertTrue(platforms.isArduino(ARDUINO_ESP8266));
        assertTrue(platforms.isArduino(STM32DUINO));
        assertTrue(platforms.isArduino(ARDUINO_AVR));
        assertFalse(platforms.isArduino(MBED_RTOS));
        assertTrue(platforms.isNativeCpp(MBED_RTOS));

        CodeGeneratorOptions standardOptions = new CodeGeneratorOptions();
        var generator = codeGenSupplier.getCodeGeneratorFor(ARDUINO_AVR, standardOptions);
        assertThat(generator).isOfAnyClassIn(ArduinoGenerator.class);

        assertThrows(IllegalArgumentException.class, () -> {
            EmbeddedPlatform invalidPlatform = new EmbeddedPlatform("", "Invalid", false);
            codeGenSupplier.getCodeGeneratorFor(invalidPlatform, standardOptions);
        });
    }
}