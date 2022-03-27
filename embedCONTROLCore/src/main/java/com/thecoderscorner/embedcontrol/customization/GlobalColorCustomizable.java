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
        public boolean isColorProvided(ConditionalColoring.ColorComponentType componentType) {
            return true;
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
    public String toString() {
        return "Global Color Settings";
    }
}