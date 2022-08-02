/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoLibraryInstaller;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.TcMenuConversionException;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.INFO;

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

    @Override
    public void internalConversion(Path directory, Path srcDir, Map<MenuItem, CallbackRequirement> callbackFunctions,
                                   String projectName) throws TcMenuConversionException {

        // get the file names that we are going to modify.
        String mainFile = Paths.get(srcDir.toString(),"tcmenu_main.cpp").toString();

        updateMbedMain(mainFile, projectName, callbackFunctions.values());
    }


    private void updateMbedMain(String mainFile, String projectName,
                                     Collection<CallbackRequirement> callbackFunctions) throws TcMenuConversionException {
        logLine(INFO, "Making adjustments to " + mainFile);

        try {
            sketchAdjuster.makeAdjustments(this::logLine, mainFile, projectName, callbackFunctions, menuTree);
        } catch (IOException e) {
            logger.log(ERROR, "Sketch modification failed", e);
            throw new TcMenuConversionException("Could not modify sketch", e);
        }
    }

}
