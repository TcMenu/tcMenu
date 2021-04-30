/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedSketchFileAdjuster;

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
    private final List<EmbeddedPlatform> platforms = List.of(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32, MBED_RTOS);
    private final List<EmbeddedPlatform> arduinoPlatforms = List.of(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32); // at the moment all platforms are Arduino.
    private final List<EmbeddedPlatform> mbedPlatforms = List.of(MBED_RTOS); // at the moment all platforms are Arduino.
    private ArduinoLibraryInstaller installer;

    public PluginEmbeddedPlatformsImpl() {
    }

    @Override
    public List<EmbeddedPlatform> getEmbeddedPlatforms() {
        return platforms;
    }

    @Override
    public CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform, CodeGeneratorOptions options) {
        if(installer == null) throw new IllegalArgumentException("Please call setInstaller first");
        if(arduinoPlatforms.contains(platform)) {
            return new ArduinoGenerator(new ArduinoSketchFileAdjuster(options), installer,
                                        platform, options);
        }
        else if(mbedPlatforms.contains(platform)) {
            return new MbedGenerator(new MbedSketchFileAdjuster(options), installer,
                                        platform, options);
        }
        else {
            throw new IllegalArgumentException("No such board type: " + platform);
        }
    }

    public boolean isArduino(EmbeddedPlatform platform) {
        return arduinoPlatforms.contains(platform);
    }

    public boolean isMbed(EmbeddedPlatform platform) {
        return mbedPlatforms.contains(platform);
    }

    @Override
    public EmbeddedPlatform getEmbeddedPlatformFromId(String id) {
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
        else if(id.equals(MBED_RTOS.getBoardId())) {
            return MBED_RTOS;
        }
        else {
            throw new IllegalArgumentException("No such board type: " + id);
        }
    }

    public void setInstaller(ArduinoLibraryInstaller installer) {
        this.installer = installer;
    }
}
