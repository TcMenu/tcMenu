package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.ejava.EmbeddedJavaGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import static com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl.*;

public class CodeGeneratorSupplier {
    private final ConfigurationStorage configStorage;
    private final ArduinoLibraryInstaller installer;

    public CodeGeneratorSupplier(ConfigurationStorage configStorage, ArduinoLibraryInstaller installer) {
        this.configStorage = configStorage;
        this.installer = installer;
    }

    public CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform, CodeGeneratorOptions options) {
        if (installer == null) throw new IllegalArgumentException("Please call setInstaller first");
        if (arduinoPlatforms.contains(platform)) {
            return new ArduinoGenerator(new ArduinoSketchFileAdjuster(options, configStorage), installer, platform);
        } else if (mbedPlatforms.contains(platform)) {
            return new MbedGenerator(new MbedSketchFileAdjuster(options, configStorage), installer, platform);
        } else if (javaPlatforms.contains(platform)) {
            return new EmbeddedJavaGenerator(configStorage, platform);
        } else {
            throw new IllegalArgumentException("No such board type: " + platform);
        }
    }
}
