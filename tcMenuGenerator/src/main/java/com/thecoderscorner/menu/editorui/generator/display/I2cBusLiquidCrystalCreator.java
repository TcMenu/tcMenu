/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.CreatorProperty;

import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.PropType;
import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public class I2cBusLiquidCrystalCreator extends LiquidCrystalCreator {

    public I2cBusLiquidCrystalCreator() {
        properties().add(new CreatorProperty("LCD_I2C_ADDR", "I2C Address of Display", "0x20", DISPLAY));
        properties().add(new CreatorProperty("EXPANDER_VARIABLE", "Optional - IoAbstractionRef, default is 8574", "", DISPLAY, PropType.TEXTUAL));
    }

    @Override
    public List<String> getIncludes() {
        return Arrays.asList("#include <Wire.h>", "#include \"tcMenuLiquidCrystal.h\"", "#include <IoAbstractionWire.h>");
    }

    @Override
    public String getExportDefinitions() {
        String additionalExports = "";
        String expVar = findPropertyValue("EXPANDER_VARIABLE").getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            additionalExports = "extern IoAbstractionRef " + expVar + ";" + LINE_BREAK;
        }

        return additionalExports + super.getExportDefinitions();
    }

    @Override
    protected String abstractionCode() {
        String expVar = findPropertyValue("EXPANDER_VARIABLE").getLatestValue();
        if(expVar != null && !expVar.isEmpty()) {
            return expVar;
        }
        else {
            return "ioFrom8574(LCD_I2C_ADDR)";
        }
    }
}
