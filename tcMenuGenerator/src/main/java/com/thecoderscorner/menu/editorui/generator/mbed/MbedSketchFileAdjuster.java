/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;
import com.thecoderscorner.menu.editorui.storage.ConfigurationStorage;

import java.nio.file.Path;
import java.util.ArrayList;

public class MbedSketchFileAdjuster extends ArduinoSketchFileAdjuster {

    public MbedSketchFileAdjuster(CodeGeneratorOptions options, ConfigurationStorage config) {
        super(options, config);
    }

    @Override
    protected String emptyFileContents() {
        return """
                #include <mbed.h>

                void setup() {

                }

                int main() {
                    setup();
                    while(1) {
                        taskManager.runLoop();
                    }
                }
                """;
    }

    protected void addIncludeToTopOfFile(ArrayList<String> lines, String projectName) {
        lines.add(1, "#include \"" + projectName + "_menu.h\"");
        changed = true;
    }

    protected void taskManagerIsMissing(ArrayList<String> lines) {
        logger.accept(System.Logger.Level.ERROR, "SKETCH ERROR: you are missing taskManager.runLoop(); in your sketch file");
        logger.accept(System.Logger.Level.ERROR, "SKETCH ERROR: this should be added in a loop within the main() method");
    }

    protected Path getCppMainPath(Path path) {
        return path.resolve("tcmenu_main.cpp");
    }

}