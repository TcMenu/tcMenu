/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

public class ArduinoPinLiquidCrystalCreator extends LiquidCrystalCreator {
    @Override
    protected String abstractionCode() {
        return "ioUsingArduino()";
    }
}
