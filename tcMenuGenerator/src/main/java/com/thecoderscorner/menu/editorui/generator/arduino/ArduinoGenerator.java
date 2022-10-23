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
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static java.lang.System.Logger.Level.*;

public class ArduinoGenerator extends CoreCodeGenerator {


    private static final String HEADER_TOP = "#ifndef MENU_GENERATED_CODE_H" + LINE_BREAK +
            "#define MENU_GENERATED_CODE_H" + LINE_BREAK + LINE_BREAK +
            "#include <Arduino.h>" + LINE_BREAK +
            "#include <tcMenu.h>" + LINE_BREAK;

    public ArduinoGenerator(SketchFileAdjuster adjuster,
                            ArduinoLibraryInstaller installer,
                            EmbeddedPlatform embeddedPlatform) {
        super(adjuster, installer, embeddedPlatform);
    }

    @Override
    protected String platformIncludes() {
        return HEADER_TOP;
    }

    private void checkIfUpToDateWarningNeeded() {
        if (!installer.statusOfAllLibraries().isUpToDate()) {
            logLine(WARNING, "WARNING==================================================================");
            logLine(WARNING, "Embedded libraries are not on recommended versions, build problems likely");
            logLine(WARNING, "WARNING==================================================================");
        }
    }


    @Override
    public void internalConversion(Path directory, Path srcDir, Map<MenuItem, CallbackRequirement> callbackFunctions, String projectName) throws TcMenuConversionException {
        super.internalConversion(directory, srcDir, callbackFunctions, projectName);
        checkIfUpToDateWarningNeeded();
    }
}
