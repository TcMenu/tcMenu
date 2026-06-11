/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.TcMenuConversionException;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

public class ArduinoGenerator extends CoreCodeGenerator {


    private static final String HEADER_TOP = """
            #ifndef MENU_GENERATED_CODE_H
            #define MENU_GENERATED_CODE_H
            
            #include <Arduino.h>
            #include <tcMenu.h>
            """;

    public ArduinoGenerator(SketchFileAdjuster adjuster,
                            EmbeddedPlatform embeddedPlatform,
                            ConfigurationStorage configStore,
                            Clock clock, UserFeedbackLogger feedbackLogger) {
        super(adjuster, embeddedPlatform, configStore, clock, feedbackLogger);
    }

    @Override
    protected String platformIncludes() {
        return HEADER_TOP;
    }
}
