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

import static java.lang.System.Logger.Level.ERROR;

public class ArduinoGenerator extends CoreCodeGenerator {


    private static final String HEADER_TOP = "#ifndef MENU_GENERATED_CODE_H" + LINE_BREAK +
            "#define MENU_GENERATED_CODE_H" + LINE_BREAK + LINE_BREAK +
            "#include <Arduino.h>" + LINE_BREAK +
            "#include <tcMenu.h>" + LINE_BREAK + LINE_BREAK;



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

        String inoFile = toSourceFile(directory, ".ino");
        updateArduinoSketch(inoFile, projectName, callbackFunctions.values());

        // do a couple of final checks and put out warnings if need be
        checkIfUpToDateWarningNeeded();
        checkIfLegacyFilesAreOnPath(directory);
    }


    private void checkIfLegacyFilesAreOnPath(Path directory) {
        if (Files.exists(Paths.get(toSourceFile(directory, ".h")))
                || Files.exists(Paths.get(toSourceFile(directory, ".cpp")))) {

            Path fileName = directory.getFileName();
            logLine("ERROR: OLD FILES FOUND !!!!!!!!!!==========================================");
            logLine("POTENTIAL COMPILE ERROR IN IDE - Non backward compatible change");
            logLine("From V1.2 onwards the source files containing menu definitions have changed");
            logLine("from " + fileName + ".h/.cpp to " + fileName + "_menu.h/_menu.cpp");
            logLine("To avoid errors in your IDE you will need to open the directory and remove");
            logLine("the files " + fileName + ".h/.cpp");
            logLine("===========================================================================");
        }
    }


    private void checkIfUpToDateWarningNeeded() {
        if (!installer.statusOfAllLibraries().isUpToDate()) {
            logLine("WARNING===============================================================");
            logLine("The embedded libraries are not up-to-date, build problems are likely");
            logLine("Select ROOT menu item and choose update libraries from the editor");
            logLine("WARNING===============================================================");
        }
    }

    private void updateArduinoSketch(String inoFile, String projectName,
                                     Collection<CallbackRequirement> callbackFunctions) throws TcMenuConversionException {
        logLine("Making adjustments to " + inoFile);

        try {
            sketchAdjuster.makeAdjustments(this::logLine, inoFile, projectName, callbackFunctions);
        } catch (IOException e) {
            logger.log(ERROR, "Sketch modification failed", e);
            throw new TcMenuConversionException("Could not modify sketch", e);
        }
    }

}
