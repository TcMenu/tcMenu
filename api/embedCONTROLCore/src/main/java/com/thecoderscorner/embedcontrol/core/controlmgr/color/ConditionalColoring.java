package com.thecoderscorner.embedcontrol.core.controlmgr.color;

import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.state.PortableColor;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

/**
 * Represents the color of a control, both background and foreground. The color can change depending on the current
 * state and component type, for example when it recently updated or if an error occurred, the full list of
 * possibilities is in the enum.
 */
public interface ConditionalColoring
{
    /** Indicates the current drawing mode of this conditional color */
    enum ColorComponentType { TEXT_FIELD, BUTTON, HIGHLIGHT, CUSTOM, DIALOG, ERROR, PENDING }

    /**
     * Gets the foreground color associated with the current state or compType.
     * @param status the current rendering status
     * @param compType the type of color to select
     * @return the color associated with the state for foreground
     */
    PortableColor foregroundFor(RenderingStatus status, ColorComponentType compType);

    /**
     * Gets the background color associated with the current state or compType.
     * @param status the current rendering status
     * @param compType the type of color to select
     * @return the color associated with the state for background
     */
    PortableColor backgroundFor(RenderingStatus status, ColorComponentType compType);

    /**
     * Gets the background and foreground color at the same time for the given state and component.
     * @param status the current rendering status
     * @param ty the type of value
     * @return the color pair
     */
    ControlColor colorFor(RenderingStatus status, ColorComponentType ty);

}