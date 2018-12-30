/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.CreatorProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public abstract class LiquidCrystalCreator extends AbstractCodeCreator {

    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("LCD_RS", "RS connection to display", "0", DISPLAY),
            new CreatorProperty("LCD_EN", "EN connection to display", "2", DISPLAY),
            new CreatorProperty("LCD_D4", "D4 connection to display", "4", DISPLAY),
            new CreatorProperty("LCD_D5", "D5 connection to display", "5", DISPLAY),
            new CreatorProperty("LCD_D6", "D6 connection to display", "6", DISPLAY),
            new CreatorProperty("LCD_D7", "D7 connection to display", "7", DISPLAY),
            new CreatorProperty("LCD_WIDTH", "Number of chars across", "20", DISPLAY),
            new CreatorProperty("LCD_HEIGHT", "Number of chars down", "4", DISPLAY),
            new CreatorProperty("LCD_PWM_PIN", "Pin for PWM contrast (-1 is off)", "-1", DISPLAY)
    ));

    @Override
    public List<String> getIncludes() {
        return Collections.singletonList("#include \"tcMenuLiquidCrystal.h\"");
    }

    @Override
    public String getGlobalVariables() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ")
                .append(abstractionCode()).append(");").append(LINE_BREAK)
                .append("LiquidCrystalRenderer renderer(lcd, LCD_WIDTH, LCD_HEIGHT);").append(LINE_BREAK);
        return sb.toString();
    }

    protected abstract String abstractionCode();

    @Override
    public List<String> getRequiredFiles() {
        return Arrays.asList("renderers/liquidcrystal/tcMenuLiquidCrystal.cpp", "renderers/liquidcrystal/tcMenuLiquidCrystal.h");
    }

    @Override
    public String getExportDefinitions() {
        return super.getExportDefinitions()
                + "extern LiquidCrystal lcd;" + LINE_BREAK
                + "extern LiquidCrystalRenderer renderer;" + LINE_BREAK;
    }

    @Override
    public String getSetupCode(String rootItem) {
        StringBuilder sb = new StringBuilder();
        sb.append("    lcd.begin(LCD_WIDTH, LCD_HEIGHT);").append(LINE_BREAK);

        if (findPropertyValue("LCD_PWM_PIN").getLatestValueAsInt() != -1) {
            sb.append("\tpinMode(LCD_PWM_PIN, OUTPUT);").append(LINE_BREAK);
            sb.append("\tanalogWrite(LCD_PWM_PIN, 10);").append(LINE_BREAK);
        }

        return sb.toString();
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
