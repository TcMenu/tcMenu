package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;
import com.thecoderscorner.embedcontrol.core.service.GlobalSettings;

public class GlobalColorCustomizable implements ColorCustomizable {
    private final GlobalSettings globalSettings;

    public GlobalColorCustomizable(GlobalSettings globalSettings) {
        this.globalSettings = globalSettings;
    }

    @Override
    public boolean isRepresentingGlobal() {
        return true;
    }

    @Override
    public ColorStatus getColorStatus(ConditionalColoring.ColorComponentType componentType) {
        return ColorStatus.AVAILABLE;
    }

    @Override
    public ControlColor getColorFor(ConditionalColoring.ColorComponentType componentType) {
        return switch (componentType) {
            case BUTTON -> globalSettings.getButtonColor();
            case TEXT_FIELD -> globalSettings.getTextColor();
            case HIGHLIGHT -> globalSettings.getHighlightColor();
            case CUSTOM -> globalSettings.getUpdateColor();
            case DIALOG -> globalSettings.getDialogColor();
            case ERROR -> globalSettings.getErrorColor();
            case PENDING -> globalSettings.getPendingColor();
        };
    }

    @Override
    public void setColorFor(ConditionalColoring.ColorComponentType componentType, ControlColor controlColor) {
        getColorFor(componentType).copyColorsFrom(controlColor);
    }

    @Override
    public void clearColorFor(ConditionalColoring.ColorComponentType componentType) {
        // not implemented on global and should never be called.
    }

    @Override
    public int getFontSize() {
        return globalSettings.getDefaultFontSize();
    }

    @Override
    public void setFontSize(int size) {
        globalSettings.setDefaultFontSize(size);
    }

    @Override
    public boolean isRecursiveRender() {
        return globalSettings.isDefaultRecursiveRendering();
    }

    @Override
    public void setRecursiveRender(boolean recursiveRender) {
        globalSettings.setDefaultRecursiveRendering(recursiveRender);
    }

    @Override
    public String toString() {
        return "Global Color Settings";
    }
}