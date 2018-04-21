package com.thecoderscorner.menu.editorui.generator.display;

public enum DisplayType {
    LCD_16_BY_2_PWM(new LiquidCrystalCreator(16, 2, true), "LiquidCrystal 16x2 PWM Contrast"),
    LCD_20_BY_4_PWM(new LiquidCrystalCreator(20, 4, true), "LiquidCrystal 20x4 PWM Contrast"),
    LCD_16_BY_2_MAN(new LiquidCrystalCreator(16, 2, false), "LiquidCrystal 16x2 Manual Contrast"),
    LCD_20_BY_4_MAN(new LiquidCrystalCreator(20, 4, false), "LiquidCrystal 20x4 Manual Contrast");

    private final DisplayCreator creator;
    private final String friendlyName;

    DisplayType(DisplayCreator creator, String friendlyName) {
        this.creator = creator;
        this.friendlyName = friendlyName;
    }

    public DisplayCreator getCreator() {
        return creator;
    }

    @Override
    public String toString() {
        return friendlyName;
    }
}
