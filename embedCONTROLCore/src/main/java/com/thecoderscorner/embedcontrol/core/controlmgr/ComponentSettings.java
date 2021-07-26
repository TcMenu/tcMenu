package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.NullConditionalColoring;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.*;

public class ComponentSettings {
    public static final ComponentSettings NO_COMPONENT = new ComponentSettings(new NullConditionalColoring(), 0, PortableAlignment.LEFT, null, RedrawingMode.SHOW_LABEL_NAME_VALUE, false);

    private final int fontSize;
    private final ConditionalColoring colors;
    private final PortableAlignment justification;
    private final ComponentPositioning position;
    private final RedrawingMode drawMode;
    private final boolean customised;

    public ComponentSettings(ConditionalColoring colors, int fontSize, PortableAlignment justification,
        ComponentPositioning position, RedrawingMode mode, boolean custom)
    {
        this.fontSize = fontSize;
        this.colors = colors;
        this.justification = justification;
        this.position = position;
        this.drawMode = mode;
        this.customised = custom;
    }

    public int getFontSize() {
        return fontSize;
    }

    public ConditionalColoring getColors() {
        return colors;
    }

    public PortableAlignment getJustification() {
        return justification;
    }

    public ComponentPositioning getPosition() {
        return position;
    }

    public RedrawingMode getDrawMode() {
        return drawMode;
    }

    public boolean isCustomised() {
        return customised;
    }
}