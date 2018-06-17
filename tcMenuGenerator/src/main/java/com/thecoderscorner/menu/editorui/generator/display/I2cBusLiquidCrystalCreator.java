/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.DISPLAY;

public class I2cBusLiquidCrystalCreator extends LiquidCrystalCreator {

    public I2cBusLiquidCrystalCreator() {
        properties().add(new CreatorProperty("LCD_I2C_ADDR", "I2C Address of Display", "0x20", DISPLAY));
    }

    @Override
    public List<String> getIncludes() {
        return Arrays.asList("#include <Wire.h>", "#include <tcMenuLiquidCrystal.h>");
    }

    @Override
    protected String abstractionCode() {
        return "ioFrom8754(LCD_I2C_ADDR)";
    }
}
