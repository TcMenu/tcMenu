package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

public interface ColorCustomizable {
    enum ColorStatus { NOT_PROVIDED, PROVIDED_NOT_AVAILABLE, AVAILABLE }

    boolean isRepresentingGlobal();
    ColorStatus getColorStatus(ColorComponentType componentType);
    ControlColor getColorFor(ColorComponentType componentType);
    default ControlColor getActualUnderlyingColor(ColorComponentType componentType) {
        return getColorFor(componentType);
    }

    void setColorFor(ColorComponentType componentType, ControlColor controlColor);
    void clearColorFor(ColorComponentType componentType);

    /**
     * Get the name of this color scheme for saving and loading.
     * @return the name of the scheme
     */
    String getColorSchemeName();
}
