package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import java.util.Optional;

public record ComponentSettingsWithMenuId(int menuId, ComponentSettings settings, Optional<ControlColor> textColor,
                                          Optional<ControlColor> buttonColor, Optional<ControlColor> updateColor,
                                          Optional<ControlColor> highlightColor) {

    public Optional<ControlColor> getColor(ConditionalColoring.ColorComponentType componentType) {
        return switch(componentType) {
            case BUTTON -> buttonColor;
            case HIGHLIGHT -> highlightColor;
            case TEXT_FIELD -> textColor;
            case CUSTOM -> updateColor;
            default -> Optional.empty();
        };
    }

}
