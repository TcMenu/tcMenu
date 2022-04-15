package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.NullConditionalColoring;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;

/**
 * When automatic menu layout is used, the layout is described in terms of font, color, position and justification using
 * this class. It can either be auto-generated on the fly, or selected by a user and serialized by the layout persister
 * for later reloading. This is essentially a value class.
 *
 * @see com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence
 */
public class ComponentSettings {
    public static final ComponentSettings NO_COMPONENT = new ComponentSettings(new NullConditionalColoring(), 0, PortableAlignment.LEFT, new ComponentPositioning(0, 0), RedrawingMode.SHOW_NAME_VALUE, ControlType.TEXT_CONTROL, false);

    private final int fontSize;
    private final ConditionalColoring colors;
    private final PortableAlignment justification;
    private final ComponentPositioning position;
    private final ControlType controlType;
    private final RedrawingMode drawMode;
    private final boolean customised;

    public ComponentSettings(ConditionalColoring colors, int fontSize, PortableAlignment justification,
        ComponentPositioning position, RedrawingMode mode, ControlType controlType, boolean custom)
    {
        this.fontSize = fontSize;
        this.colors = colors;
        this.justification = justification;
        this.position = position;
        this.drawMode = mode;
        this.customised = custom;
        this.controlType = controlType;
    }

    /**
     * @return the font size for any text in the control
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * @return the foreground and background colors to use for rendering.
     */
    public ConditionalColoring getColors() {
        return colors;
    }

    /**
     * @return the alignment/justification for the control
     */
    public PortableAlignment getJustification() {
        return justification;
    }

    /**
     * @return the grid position for the control in terms of rows and columns
     */
    public ComponentPositioning getPosition() {
        return position;
    }

    /**
     *
     * @return the redrawing mode, EG if the name only is presented, or the name and value, or just the value.
     */
    public RedrawingMode getDrawMode() {
        return drawMode;
    }

    /**
     * @return the type of control to use for this component.
     */
    public ControlType getControlType() {
        return controlType;
    }

    /**
     * Indicates if this setting has been customized
     * @return true if customized otherwise false.
     */
    public boolean isCustomised() {
        return customised;
    }
}