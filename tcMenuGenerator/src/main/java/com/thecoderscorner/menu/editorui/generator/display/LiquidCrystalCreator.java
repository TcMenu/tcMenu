package com.thecoderscorner.menu.editorui.generator.display;

import java.util.Collections;
import java.util.List;

public class LiquidCrystalCreator implements DisplayCreator{

    private final int width;
    private final int height;
    private final boolean pwm;

    public LiquidCrystalCreator(int width, int height, boolean pwm) {
        this.width = width;
        this.height = height;
        this.pwm = pwm;
    }

    @Override
    public List<String> getIncludes() {
        return Collections.singletonList("#include <LiquidCrystal.h>");
    }

    @Override
    public String getGlobalVariables() {
        StringBuilder sb = new StringBuilder(1024);
        sb.append("#define LCD_RS 1\n");
        sb.append("#define LCD_EN 2\n");
        sb.append("#define LCD_D4 3\n");
        sb.append("#define LCD_D5 4\n");
        sb.append("#define LCD_D6 5\n");
        sb.append("#define LCD_D7 6\n");
        if(pwm) {
            sb.append("#define LCD_PWM_CONTRAST 5\n");
        }
        sb.append("LiquidCrystal lcd(LCD_RS, LCD_EN, LCD_D4, LCD_D5, LCD_D6, LCD_D7, ioFrom8754(0x20)); // ioUsingArduino() for non i2c\n");
        sb.append("LiquidCrystalRenderer renderer(lcd, ").append(width).append(", ").append(height).append(");");
        return sb.toString();
    }

    @Override
    public String getSetupCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("    lcd.begin(").append(width).append(", ").append(height).append(");\n");
        if(pwm) {
            sb.append("// PWM contrast support, set contrast pin further up..\n");
            sb.append("\tpinMode(LCD_PWM_CONTRAST, OUTPUT);\n");
            sb.append("\tanalogWrite(LCD_PWM_CONTRAST, 10);\n");
        }
        return sb.toString();
    }
}
