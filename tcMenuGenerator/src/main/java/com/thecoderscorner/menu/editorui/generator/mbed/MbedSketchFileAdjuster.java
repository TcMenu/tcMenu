/*
 * Copyright (c)  2016-2020 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.mbed;

import com.thecoderscorner.menu.editorui.generator.CodeGeneratorOptions;
import com.thecoderscorner.menu.editorui.generator.arduino.ArduinoSketchFileAdjuster;

import java.util.ArrayList;

public class MbedSketchFileAdjuster extends ArduinoSketchFileAdjuster {

    public MbedSketchFileAdjuster(CodeGeneratorOptions options) {
        super(options);
    }

    @Override
    protected String emptyFileContents() {
        return "#include <mbed.h>\n" +
                "\n" +
                "void setup() {\n" +
                "\n" +
                "}\n" +
                "\n" +
                "int main() {\n" +
                "    setup();\n" +
                "    while(1) {\n" +
                "        taskManager.runLoop();\n" +
                "    }\n" +
                "}\n";
    }

    protected void addIncludeToTopOfFile(ArrayList<String> lines, String projectName) {
        lines.add(1, "#include \"" + projectName + "_menu.h\"");
        changed = true;
    }

    protected void taskManagerIsMissing(ArrayList<String> lines) {
        logger.accept("SKETCH ERROR: you are missing taskManager.runLoop(); in your sketch file");
        logger.accept("SKETCH ERROR: this should be added in a loop within the main() method");
    }
}