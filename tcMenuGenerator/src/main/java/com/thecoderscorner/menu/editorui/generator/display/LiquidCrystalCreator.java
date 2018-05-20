/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.INPUT;

public class LiquidCrystalCreator extends AbstractCodeCreator {

    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LCD_RS", "RS connection to display", "0", INPUT),
            new CreatorProperty("LCD_EN", "EN connection to display", "0", INPUT),
            new CreatorProperty("LCD_D4", "D4 connection to display", "0", INPUT),
            new CreatorProperty("LCD_D5", "D5 connection to display", "0", INPUT),
            new CreatorProperty("LCD_D6", "D6 connection to display", "0", INPUT),
            new CreatorProperty("LCD_D7", "D7 connection to display", "0", INPUT),
            new CreatorProperty("LCD_WIDTH", "Number of chars across", "20", INPUT),
            new CreatorProperty("LCD_HEIGHT", "Number of chars down", "4", INPUT),
            new CreatorProperty("LCD_PWM_PIN", "Pin for PWM contrast (-1 is off)", "4", INPUT)
    );

    @Override
    public List<String> getIncludes() {
        return Collections.singletonList("#include <LiquidCrystalIO.h>");
    }

    @Override
    public String getGlobalVariables() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ioFrom8754(0x20)); // ioUsingArduino() for non i2c\n");
        sb.append("LiquidCrystalRenderer renderer(lcd, LCD_WIDTH, LCD_HEIGHT);");
        return sb.toString();
    }

    @Override
    public String getExportDefinitions() {
        return "extern LiquidCrystal lcd;" + LINE_BREAK + "extern LiquidCrystalRenderer renderer;" + LINE_BREAK;
    }

    @Override
    public String getSetupCode(String rootItem) {
        StringBuilder sb = new StringBuilder();
        sb.append("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);").append(LINE_BREAK);

        if (findPropertyValue("LCD_PWM_PIN").getLatestValueAsInt() != -1) {
            sb.append("\tpinMode(LCD_PWM_PIN, OUTPUT);\n");
            sb.append("\tanalogWrite(LCD_PWM_PIN, 10);\n");
        }

        return sb.toString();
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
