package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.state.PortableColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public interface ConditionalColoring
{
    enum ColorComponentType { TEXT_FIELD, BUTTON, HIGHLIGHT, CUSTOM }

    PortableColor foregroundFor(RenderingStatus status, ColorComponentType compType);
    PortableColor backgroundFor(RenderingStatus status, ColorComponentType compType);
}