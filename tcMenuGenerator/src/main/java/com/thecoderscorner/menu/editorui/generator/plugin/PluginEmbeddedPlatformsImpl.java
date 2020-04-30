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
    private final List<EmbeddedPlatform> platforms = List.of(ARDUINO_AVR, ARDUINO32, ARDUINO_ESP8266, ARDUINO_ESP32);
    private final List<EmbeddedPlatform> arduinoPlatforms = platforms; // at the moment all platforms are Arduino.

    @Override
    public List<EmbeddedPlatform> getEmbeddedPlatforms() {
        return platforms;
    }

    @Override
    public CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform, CodeGeneratorOptions options) {
        if(arduinoPlatforms.contains(platform)) {
            return new ArduinoGenerator(new ArduinoSketchFileAdjuster(), new ArduinoLibraryInstaller(),
                                        platform, options);
        }
        else {
            throw new IllegalArgumentException("No such board type: " + platform);
        }
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
        else {
            throw new IllegalArgumentException("No such board type: " + id);
        }
    }
}
