package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.util.DataException;
import com.thecoderscorner.embedcontrol.core.util.TccDatabaseUtilities;
import com.thecoderscorner.embedcontrol.customization.ApplicationThemeManager;

import java.util.Map;
import java.util.UUID;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType.*;
import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor.*;

/**
 * This class stores all the global settings for both local and remote versions of embedCONTROL. This includes default
 * font sizes, control colors, and other setup values.
 */
public class GlobalSettings {
    private static int globalFontSize = 14;

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
    private int defaultFontSize;
    private ApplicationThemeManager themeManager;

    /**
     * Create an instance of global settings
     */
    public GlobalSettings(ApplicationThemeManager themeManager) {
        this.themeManager = themeManager;
        appUuid = UUID.randomUUID().toString();
        appName = "untitled";
        defaultFontSize = 16;
        resetColorsToDefault();
    }

    public static int defaultFontSize() {
        return globalFontSize;
    }

    public void copyFrom(GlobalSettings other) {
        this.appUuid = other.appUuid;
        this.appName = other.appName;
        this.defaultFontSize = other.defaultFontSize;
        globalFontSize = defaultFontSize;
        this.defaultRecursiveRendering = other.defaultRecursiveRendering;
        this.buttonColor.copyColorsFrom(other.buttonColor);
        this.updateColor.copyColorsFrom(other.updateColor);
        this.updateColor.copyColorsFrom(other.highlightColor);
        this.updateColor.copyColorsFrom(other.textColor);
        this.updateColor.copyColorsFrom(other.errorColor);
        this.updateColor.copyColorsFrom(other.dialogColor);
        this.updateColor.copyColorsFrom(other.pendingColor);
    }

    private ControlColor wrapWithDefault(ConditionalColoring.ColorComponentType componentType, ControlColor controlColor) {
        if(controlColor.isInUse()) return controlColor;
        var colorMap = isDarkMode() ? DEFAULT_DARK_COLORS : DEFAULT_LIGHT_COLORS;
        return colorMap.containsKey(componentType) ? colorMap.get(componentType) : new ControlColor(WHITE, BLACK);
    }

    /**
     * @return the default control color for textual items
     */
    public ControlColor getTextColor() {
        return wrapWithDefault(TEXT_FIELD, textColor);
    }

    /**
     * @return the default control color for button items
     */
    public ControlColor getUpdateColor() {
        return wrapWithDefault(CUSTOM, updateColor);
    }

    /**
     * @return the default control color for pending items
     */
    public ControlColor getPendingColor() {
        return wrapWithDefault(PENDING, pendingColor);
    }

    /**
     * @return the default control color for highlight items
     */
    public ControlColor getHighlightColor() {
        return wrapWithDefault(HIGHLIGHT, highlightColor);
    }

    /**
     * @return the default control color for error items
     */
    public ControlColor getErrorColor() {
        return wrapWithDefault(ERROR, errorColor);
    }

    /**
     * @return the default control color for button items
     */
    public ControlColor getButtonColor() {
        return wrapWithDefault(BUTTON, buttonColor);
    }

    /**
     * @return the default control color for dialogs
     */
    public ControlColor getDialogColor() {
        return wrapWithDefault(DIALOG, dialogColor);
    }

    public ControlColor getUnderlyingColor(ConditionalColoring.ColorComponentType ty) {
        return switch (ty) {
            case TEXT_FIELD -> textColor;
            case BUTTON -> buttonColor;
            case HIGHLIGHT -> highlightColor;
            case CUSTOM -> updateColor;
            case DIALOG -> dialogColor;
            case ERROR -> errorColor;
            case PENDING -> pendingColor;
        };
    }

    public Map<ConditionalColoring.ColorComponentType, ControlColor> getColorsToSave() {
        return Map.of(
                CUSTOM, updateColor,
                PENDING, pendingColor,
                HIGHLIGHT, highlightColor,
                ERROR, errorColor,
                BUTTON, buttonColor,
                DIALOG, dialogColor,
                TEXT_FIELD, textColor
        );
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
     * This is used to reset colorschemes back to the default settings.
     */
    public void resetColorsToDefault() {
        updateColor = new ControlColor();
        pendingColor = new ControlColor();
        buttonColor = new ControlColor();
        errorColor = new ControlColor();
        highlightColor = new ControlColor();
        dialogColor = new ControlColor();
        textColor = new ControlColor();
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
        globalFontSize = size;
        defaultFontSize = size;
    }

    /**
     * Set recursive rendering on or off by default
     * @param recursiveRender the new state
     */
    public void setDefaultRecursiveRendering(boolean recursiveRender) {
        defaultRecursiveRendering = recursiveRender;
    }

    public boolean isDarkMode() {
        return themeManager.isDarkMode();
    }

    public void save(TccDatabaseUtilities databaseUtilities) {
        TcPreferencesPersistence pp = new TcPreferencesPersistence(this);
        try {
            databaseUtilities.updateRecord(TcPreferencesPersistence.class, pp);
        } catch (DataException e) {
            throw new RuntimeException("Failed up update global settings record");
        }
    }
}
