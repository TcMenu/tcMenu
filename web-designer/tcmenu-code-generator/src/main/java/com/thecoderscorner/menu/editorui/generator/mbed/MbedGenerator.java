/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.logger.UserFeedbackLogger;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.time.Clock;

public class MbedGenerator extends CoreCodeGenerator {
    public MbedGenerator(SketchFileAdjuster adjuster,
                         EmbeddedPlatform embeddedPlatform,
                         ConfigurationStorage config,
                         Clock clock, UserFeedbackLogger userFeedbackLogger) {
        super(adjuster, embeddedPlatform, config, clock, userFeedbackLogger);
    }

    private static final String HEADER_TOP = """
            #ifndef MENU_GENERATED_CODE_H
            #define MENU_GENERATED_CODE_H
            
            #include <PlatformDetermination.h>
            #include <tcMenu.h>
            
            """;


    @Override
    protected String platformIncludes() {
        return HEADER_TOP;
    }
}
