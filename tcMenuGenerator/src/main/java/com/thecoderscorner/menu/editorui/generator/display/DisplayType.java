/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.EnumWithApplicability;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DisplayType extends EnumWithApplicability {

    public static Map<Integer, DisplayType> values = new HashMap<>();

    static {
        addValue(1, Set.of(EmbeddedPlatform.ARDUINO), "No Display", DisplayNotUsedCreator.class);
        addValue(2, Set.of(EmbeddedPlatform.ARDUINO), "LiquidCrystalIO Arduino Pins", ArduinoPinLiquidCrystalCreator.class);
        addValue(3, Set.of(EmbeddedPlatform.ARDUINO), "LiquidCrystalIO on i2c bus", I2cBusLiquidCrystalCreator.class);
        addValue(4, Set.of(EmbeddedPlatform.ARDUINO), "Adafruit_GFX Display", AdafruitGfxDisplayCreator.class);
    }

    public DisplayType(Set<EmbeddedPlatform> platformApplicability, String description,
                       Class<? extends EmbeddedCodeCreator> creator, int key) {
        super(platformApplicability, description, creator, key);
    }

    private static void addValue(int key, Set<EmbeddedPlatform> applicability, String description,
                                 Class<? extends EmbeddedCodeCreator> creator) {
        values.put(key, new DisplayType(applicability, description, creator, key));
    }

}
