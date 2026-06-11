package com.thecoderscorner.embedcontrol.customization;

public class ApplicationThemeManager {
    private String themeName;

    public ApplicationThemeManager() {
        themeName = "lightMode";
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public boolean isDarkMode() {
        return "darkMode".equals(themeName);
    }
}
