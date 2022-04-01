package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ControlColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring.ColorComponentType;

public interface ColorCustomizable {
    enum ColorStatus { NOT_PROVIDED, PROVIDED_NOT_AVAILABLE, AVAILABLE }

    boolean isRepresentingGlobal();
    ColorStatus getColorStatus(ColorComponentType componentType);
    ControlColor getColorFor(ColorComponentType componentType);
    void setColorFor(ColorComponentType componentType, ControlColor controlColor);
    void clearColorFor(ColorComponentType componentType);
    int getFontSize();
    void setFontSize(int size);
    boolean isRecursiveRender();
    void setRecursiveRender(boolean recursiveRender);

}
