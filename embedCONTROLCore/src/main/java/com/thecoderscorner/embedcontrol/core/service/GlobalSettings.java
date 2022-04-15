package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.util.StringHelper;
import com.thecoderscorner.menu.domain.state.PortableColor;

import java.util.UUID;
import java.util.prefs.Preferences;

/**
 * This class stores all the global settings for both local and remote versions of embedCONTROL. This includes default
 * font sizes, control colors, and other setup values. When creating an instance provide a class that you'd like to use
 * as the root node for the preferences API.
 */
public class GlobalSettings {
    private final Class<?> preferencesNode;
    private ControlColor updateColor;
    private ControlColor pendingColor;
    private ControlColor highlightColor;
    private ControlColor errorColor;
    private ControlColor buttonColor;
    private ControlColor dialogColor;
    private ControlColor textColor;
    private String appUuid;
    private String appName;
    private boolean defaultRecursiveRendering;
    private boolean setupLayoutModeEnabled;
    private int defaultFontSize;

    /**
     * Create an instance of global settings that reads and stores values to preferences under the node provided
     * @param preferencesNode this is used as the root element in the preferences API
     */
    public GlobalSettings(Class<?> preferencesNode) {
        setColorsForDefault(false);
        appUuid = UUID.randomUUID().toString();
        appName = "untitled";
        defaultFontSize = 16;
        this.preferencesNode = preferencesNode;
    }

    /**
     * @return the default control color for textual items
     */
    public ControlColor getTextColor() {
        return textColor;
    }

    /**
     * @return the default control color for button items
     */
    public ControlColor getUpdateColor() {
        return updateColor;
    }

    /**
     * @return the default control color for pending items
     */
    public ControlColor getPendingColor() {
        return pendingColor;
    }

    /**
     * @return the default control color for highlight items
     */
    public ControlColor getHighlightColor() {
        return highlightColor;
    }

    /**
     * @return the default control color for error items
     */
    public ControlColor getErrorColor() {
        return errorColor;
    }

    /**
     * @return the default control color for button items
     */
    public ControlColor getButtonColor() {
        return buttonColor;
    }

    /**
     * @return the default control color for dialogs
     */
    public ControlColor getDialogColor() {
        return dialogColor;
    }

    /**
     * @return the applications UUID
     */
    public String getAppUuid() {
        return appUuid;
    }

    /**
     * Sets the applications UUID
     * @param appUuid the new UUID
     */
    public void setAppUuid(String appUuid) {
        this.appUuid = appUuid;
    }

    /**
     * @return the applications name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the applications name
     * @param appName the new name
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Loads the previous preferences back from storage at start up
     */
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
        defaultFontSize = prefs.getInt("defaultFontSize", 16);
        defaultRecursiveRendering = prefs.getBoolean("defaultRecursiveRender", false);
        setupLayoutModeEnabled = prefs.getBoolean("setupLayoutModeEnabled", false);
    }

    private void populateColorIfPresent(Preferences prefs, String colorName, ControlColor colorInfo) {
        var fg = prefs.get(colorName + "FgColor", "");
        var bg = prefs.get(colorName + "BgColor", "");
        if(StringHelper.isStringEmptyOrNull(fg) || StringHelper.isStringEmptyOrNull(bg)) return;
        colorInfo.setFg(new PortableColor(fg));
        colorInfo.setBg(new PortableColor(bg));
    }

    /**
     * Saves all preferences back to storage
     */
    public void save() {
        Preferences prefs = Preferences.userNodeForPackage(preferencesNode);
        saveColor(prefs, "update", updateColor);
        saveColor(prefs, "error", errorColor);
        saveColor(prefs, "pending", pendingColor);
        saveColor(prefs, "button", buttonColor);
        saveColor(prefs, "highlight", highlightColor);
        saveColor(prefs, "text", textColor);

        prefs.put("appUUID", appUuid);
        prefs.put("appName", appName);
        prefs.putBoolean("defaultRecursiveRender", defaultRecursiveRendering);
        prefs.putInt("defaultFontSize", defaultFontSize);
        prefs.putBoolean("setupLayoutModeEnabled", setupLayoutModeEnabled);
    }

    private void saveColor(Preferences prefs, String name, ControlColor colorData) {
        prefs.put(name + "FgColor", colorData.getFg().toString());
        prefs.put(name + "BgColor", colorData.getBg().toString());
    }

    /**
     * This is used to reset colorschemes back to the default settings.
     * @param darkMode if dark background colors should be used
     */
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

    /**
     * @return the default font size
     */
    public int getDefaultFontSize() {
        return defaultFontSize;
    }

    /**
     * @return if the system should default to recursive rendering.
     */
    public boolean isDefaultRecursiveRendering() {
        return defaultRecursiveRendering;
    }

    /**
     * Set the font size to a new value
     * @param size the font size
     */
    public void setDefaultFontSize(int size) {
        defaultFontSize = size;
    }

    /**
     * Set recursive rendering on or off by default
     * @param recursiveRender the new state
     */
    public void setDefaultRecursiveRendering(boolean recursiveRender) {
        defaultRecursiveRendering = recursiveRender;
    }

    /**
     * @return indicates if setup layout mode is enabled for editing screen layouts
     */
    public boolean isSetupLayoutModeEnabled() {
        return setupLayoutModeEnabled;
    }

    /**
     * Set screen layout editing on or off
     * @param setupLayoutModeEnabled the new state
     */
    public void setSetupLayoutModeEnabled(boolean setupLayoutModeEnabled) {
        this.setupLayoutModeEnabled = setupLayoutModeEnabled;
    }
}
