/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.ejava.EmbeddedJavaGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform.*;

/**
 * This implementation of the embedded platforms creator has now been broken out in such a way that as there
 * are other board types, it will be easier to support them by means of plugins. In this release only arduino is
 * supported, and it is still wired into the main application. However, moving it into a plugin if needed is now
 * trivial.
 *
 */
public class PluginEmbeddedPlatformsImpl implements EmbeddedPlatforms {
    private final List<EmbeddedPlatform> platforms = List.of(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, STM32DUINO, RASPBERRY_PIJ, MBED_RTOS);
    public static final List<EmbeddedPlatform> arduinoPlatforms = List.of(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, STM32DUINO);
    public static final List<EmbeddedPlatform> mbedPlatforms = List.of(MBED_RTOS);
    public static final List<EmbeddedPlatform> javaPlatforms = List.of(RASPBERRY_PIJ);

    public PluginEmbeddedPlatformsImpl() {
    }

    @Override
    public List<EmbeddedPlatform> getEmbeddedPlatforms() {
        return platforms;
    }

    public boolean isArduino(EmbeddedPlatform platform) {
        return arduinoPlatforms.contains(platform);
    }

    public boolean isMbed(EmbeddedPlatform platform) {
        return mbedPlatforms.contains(platform);
    }

    public boolean isJava(EmbeddedPlatform platform) {
        return javaPlatforms.contains(platform);
    }

    @Override
    public EmbeddedPlatform getEmbeddedPlatformFromId(String id) {
        // safe for any situation.
        if(id == null) return ARDUINO_AVR;

        // at least attempt to handle the newer definitions on the line below from the new C# UI.
        if(id.equals(ARDUINO_AVR.getBoardId()) || id.equals("ARDUINO_UNO") || id.equals("ARDUINO_AVR")) {
            return ARDUINO_AVR;
        }
        else if(id.equals(ARDUINO32.getBoardId()) || id.equals("ARDUINO_32")) {
            return ARDUINO32;
        }
        else if(id.equals(ARDUINO_ESP8266.getBoardId())) {
            return ARDUINO_ESP8266;
        }
        else if(id.equals(ARDUINO_ESP32.getBoardId())) {
            return ARDUINO_ESP32;
        }
        else if(id.equals(STM32DUINO.getBoardId())) {
            return STM32DUINO;
        }
        else if(id.equals(MBED_RTOS.getBoardId())) {
            return MBED_RTOS;
        }
        else if(id.equals(RASPBERRY_PIJ.getBoardId())) {
            return RASPBERRY_PIJ;
        }
        else {
            throw new IllegalArgumentException("No such board type: " + id);
        }
    }
}
