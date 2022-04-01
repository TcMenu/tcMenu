package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.menu.domain.state.PortableColor;

import java.util.UUID;
import java.util.prefs.Preferences;

public class GlobalSettings {
    private final Class<?> preferencesNode;
    private ControlColor updateColor;
    private ControlColor pendingColor;
    private ControlColor highlightColor;
    private ControlColor errorColor;
    private ControlColor buttonColor;
    private ControlColor dialogColor;
    private ControlColor textColor;
    private boolean darkMode;
    private String appUuid;
    private String appName;
    private boolean defaultRecursiveRendering;
    private int defaultFontSize;

    public GlobalSettings(Class<?> preferencesNode) {
        setColorsForDefault(false);
        appUuid = UUID.randomUUID().toString();
        appName = "untitled";
        defaultFontSize = 16;
        this.preferencesNode = preferencesNode;
    }

    public ControlColor getTextColor() {
        return textColor;
    }

    public ControlColor getUpdateColor() {
        return updateColor;
    }

    public ControlColor getPendingColor() {
        return pendingColor;
    }

    public ControlColor getHighlightColor() {
        return highlightColor;
    }

    public ControlColor getErrorColor() {
        return errorColor;
    }

    public ControlColor getButtonColor() {
        return buttonColor;
    }

    public ControlColor getDialogColor() {
        return dialogColor;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    public String getAppUuid() {
        return appUuid;
    }

    public void setAppUuid(String appUuid) {
        this.appUuid = appUuid;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void load() {
        Preferences prefs = Preferences.userNodeForPackage(preferencesNode);
        populateColorIfPresent(prefs,"update", updateColor);
        populateColorIfPresent(prefs,"error", errorColor);
        populateColorIfPresent(prefs,"pending", pendingColor);
        populateColorIfPresent(prefs,"button", buttonColor);
        populateColorIfPresent(prefs,"highlight", highlightColor);
        populateColorIfPresent(prefs,"text", textColor);

        appName = prefs.get("appName", "unknown");
        appUuid = prefs.get("appUUID", UUID.randomUUID().toString());
        darkMode = prefs.getBoolean("darkMode", false);
        defaultFontSize = prefs.getInt("defaultFontSize", 16);
        defaultRecursiveRendering = prefs.getBoolean("defaultRecursiveRender", false);
    }

    private void populateColorIfPresent(Preferences prefs, String colorName, ControlColor colorInfo) {
        var fg = prefs.get(colorName + "FgColor", "");
        var bg = prefs.get(colorName + "BgColor", "");
        if(StringHelper.isStringEmptyOrNull(fg) || StringHelper.isStringEmptyOrNull(bg)) return;
        colorInfo.setFg(new PortableColor(fg));
        colorInfo.setBg(new PortableColor(bg));
    }

    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(preferencesNode);
        saveColor(prefs, "update", updateColor);
        saveColor(prefs, "error", errorColor);
        saveColor(prefs, "pending", pendingColor);
        saveColor(prefs, "button", buttonColor);
        saveColor(prefs, "highlight", highlightColor);
        saveColor(prefs, "text", textColor);

        prefs.putBoolean("darkMode", darkMode);
        prefs.put("appUUID", appUuid);
        prefs.put("appName", appName);
        prefs.putBoolean("defaultRecursiveRender", defaultRecursiveRendering);
        prefs.putInt("defaultFontSize", defaultFontSize);
    }

    private void saveColor(Preferences prefs, String name, ControlColor colorData) {
        prefs.put(name + "FgColor", colorData.getFg().toString());
        prefs.put(name + "BgColor", colorData.getBg().toString());
    }

    public void setColorsForDefault(boolean darkMode) {
        if(darkMode) {
            updateColor = new ControlColor(ControlColor.WHITE, ControlColor.DARK_SLATE_BLUE);
            pendingColor = new ControlColor(ControlColor.LIGHT_GRAY, ControlColor.DARK_GREY);
            buttonColor = new ControlColor(ControlColor.WHITE, ControlColor.DARK_BLUE);
            errorColor = new ControlColor(ControlColor.WHITE, ControlColor.RED);
            highlightColor = new ControlColor(ControlColor.WHITE, ControlColor.CRIMSON);
            dialogColor = new ControlColor(ControlColor.WHITE, ControlColor.DARK_SLATE_BLUE);
            textColor = new ControlColor(ControlColor.ANTIQUE_WHITE, ControlColor.BLACK);
        }
        else
        {
            updateColor = new ControlColor(ControlColor.WHITE, ControlColor.INDIGO);
            pendingColor = new ControlColor(ControlColor.LIGHT_GRAY, ControlColor.GREY);
            buttonColor = new ControlColor(ControlColor.BLACK, ControlColor.CORNFLOWER_BLUE);
            errorColor = new ControlColor(ControlColor.WHITE, ControlColor.RED);
            highlightColor = new ControlColor(ControlColor.WHITE, ControlColor.DARK_SLATE_BLUE);
            dialogColor = new ControlColor(ControlColor.WHITE, ControlColor.DARK_SLATE_BLUE);
            textColor = new ControlColor(ControlColor.BLACK, ControlColor.ANTIQUE_WHITE);
        }
    }

    public int getDefaultFontSize() {
        return defaultFontSize;
    }

    public boolean isDefaultRecursiveRendering() {
        return defaultRecursiveRendering;
    }

    public void setDefaultFontSize(int size) {
        defaultFontSize = size;
    }

    public void setDefaultRecursiveRendering(boolean recursiveRender) {
        defaultRecursiveRendering = recursiveRender;
    }
}
