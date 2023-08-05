package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import java.util.UUID;

/**
 * This class stores all the global settings for both local and remote versions of embedCONTROL. This includes default
 * font sizes, control colors, and other setup values.
 */
public class GlobalSettings {
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

    /**
     * Create an instance of global settings
     */
    public GlobalSettings() {
        setColorsForDefault(false);
        appUuid = UUID.randomUUID().toString();
        appName = "untitled";
        defaultFontSize = 16;
    }

    public void copyFrom(GlobalSettings other) {
        this.appUuid = other.appUuid;
        this.appName = other.appName;
        this.defaultFontSize = other.defaultFontSize;
        this.defaultRecursiveRendering = other.defaultRecursiveRendering;
        this.buttonColor.copyColorsFrom(other.buttonColor);
        this.updateColor.copyColorsFrom(other.updateColor);
        this.updateColor.copyColorsFrom(other.highlightColor);
        this.updateColor.copyColorsFrom(other.textColor);
        this.updateColor.copyColorsFrom(other.errorColor);
        this.updateColor.copyColorsFrom(other.dialogColor);
        this.updateColor.copyColorsFrom(other.pendingColor);
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
}
