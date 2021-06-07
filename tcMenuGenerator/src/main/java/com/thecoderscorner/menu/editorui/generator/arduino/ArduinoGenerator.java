/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.core.CoreCodeGenerator;
import com.thecoderscorner.menu.editorui.generator.core.SketchFileAdjuster;
import com.thecoderscorner.menu.editorui.generator.core.TcMenuConversionException;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;

import static java.lang.System.Logger.Level.*;

public class ArduinoGenerator extends CoreCodeGenerator {


    private static final String HEADER_TOP = "#ifndef MENU_GENERATED_CODE_H" + LINE_BREAK +
            "#define MENU_GENERATED_CODE_H" + LINE_BREAK + LINE_BREAK +
            "#include <Arduino.h>" + LINE_BREAK +
            "#include <tcMenu.h>" + LINE_BREAK;

    public ArduinoGenerator(SketchFileAdjuster adjuster,
                            ArduinoLibraryInstaller installer,
                            EmbeddedPlatform embeddedPlatform,
                            CodeGeneratorOptions options) {
        super(adjuster, installer, embeddedPlatform, options);
    }

    @Override
    protected String platformIncludes() {
        return HEADER_TOP;
    }

    @Override
    public void internalConversion(Path directory, Path srcDir, Map<MenuItem, CallbackRequirement> callbackFunctions,
                                   String projectName) throws TcMenuConversionException {

        String inoFile;
        var path = options.isSaveToSrc() ? srcDir : directory;
        if(options.isUseCppMain()) {
            inoFile = toSourceFile(path, "_main.cpp");
        }
        else {
            inoFile = toSourceFile(path, ".ino");
        }
        updateArduinoSketch(inoFile, projectName, callbackFunctions.values());

        // do a couple of final checks and put out warnings if need be
        checkIfUpToDateWarningNeeded();
    }

    private void checkIfUpToDateWarningNeeded() {
        if (!installer.statusOfAllLibraries().isUpToDate()) {
            logLine(WARNING, "WARNING===============================================================");
            logLine(WARNING, "The embedded libraries are not up-to-date, build problems are likely");
            logLine(WARNING, "Select ROOT menu item and choose update libraries from the editor");
            logLine(WARNING, "WARNING===============================================================");
        }
    }

    private void updateArduinoSketch(String inoFile, String projectName,
                                     Collection<CallbackRequirement> callbackFunctions) throws TcMenuConversionException {
        logLine(INFO, "Making adjustments to " + inoFile);

        try {
            sketchAdjuster.makeAdjustments(this::logLine, inoFile, projectName, callbackFunctions);
        } catch (IOException e) {
            logger.log(ERROR, "Sketch modification failed", e);
            throw new TcMenuConversionException("Could not modify sketch", e);
        }
    }

}
