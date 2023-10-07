package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.menu.domain.state.PortableColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.RenderingStatus;

/**
 * Any empty implementation of conditional coloring that returns WHITE background, BLACK text always.
 */
public class NullConditionalColoring implements ConditionalColoring {
    @Override
    public PortableColor backgroundFor(RenderingStatus status, ColorComponentType ty) {
        return ControlColor.WHITE;
    }

    @Override
    public ControlColor colorFor(RenderingStatus status, ColorComponentType ty) {
        return new ControlColor(ControlColor.BLACK, ControlColor.WHITE);
    }

    @Override
    public PortableColor foregroundFor(RenderingStatus status, ColorComponentType ty) {
        return ControlColor.BLACK;
    }
}