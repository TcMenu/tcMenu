package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoGenerator;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.CodeGenerator;
import com.thecoderscorner.menu.editorui.generator.logger.DelegatingUserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedGenerator;
import com.thecoderscorner.menu.editorui.generator.mbed.MbedSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.time.Clock;

import static com.thecoderscorner.menu.editorui.generator.plugin.PluginEmbeddedPlatformsImpl.*;

public class CodeGeneratorSupplier {
    private final ConfigurationStorage configStorage;

    public CodeGeneratorSupplier(ConfigurationStorage configStorage) {
        this.configStorage = configStorage;
    }

    public CodeGenerator getCodeGeneratorFor(EmbeddedPlatform platform, CodeGeneratorOptions options, UserFeedbackLogger logger) {
        var userLoggerDelegate = new DelegatingUserFeedbackLogger(logger);
        if (arduinoPlatforms.contains(platform)) {
            return new ArduinoGenerator(new ArduinoSketchFileAdjuster(options, configStorage), platform, configStorage, Clock.systemDefaultZone(), userLoggerDelegate);
        } else if (trueCppPlatform.contains(platform)) {
            return new MbedGenerator(new MbedSketchFileAdjuster(options, configStorage), platform, configStorage, Clock.systemDefaultZone(), userLoggerDelegate);
        } else {
            throw new IllegalArgumentException("No such board type: " + platform);
        }
    }
}
