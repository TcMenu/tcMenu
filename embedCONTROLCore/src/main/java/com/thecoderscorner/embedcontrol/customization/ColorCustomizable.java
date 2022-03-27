package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import java.util.Optional;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.*;

public interface ColorCustomizable {
    boolean isRepresentingGlobal();
    boolean isColorProvided(ColorComponentType componentType);
    ControlColor getColorFor(ColorComponentType componentType);
    void setColorFor(ColorComponentType componentType, ControlColor controlColor);
}
