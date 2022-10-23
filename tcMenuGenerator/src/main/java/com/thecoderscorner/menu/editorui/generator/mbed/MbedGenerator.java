/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;

public class MbedGenerator extends CoreCodeGenerator {
    public MbedGenerator(SketchFileAdjuster adjuster, ArduinoLibraryInstaller installer, EmbeddedPlatform embeddedPlatform) {
        super(adjuster, installer, embeddedPlatform);
    }

    private static final String HEADER_TOP = "#ifndef MENU_GENERATED_CODE_H" + LINE_BREAK +
            "#define MENU_GENERATED_CODE_H" + LINE_BREAK + LINE_BREAK +
            "#include <mbed.h>" + LINE_BREAK +
            "#include <tcMenu.h>" + LINE_BREAK + LINE_BREAK;


    @Override
    protected String platformIncludes() {
        return HEADER_TOP;
    }
}
