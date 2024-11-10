package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.core.controlmgr.color.NullConditionalColoring;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.core.controlmgr.RedrawingMode.SHOW_NAME_VALUE;
import static com.thecoderscorner.embedcontrol.customization.MenuFormItem.FONT_100_PERCENT;

/// This class describes how a menu item should be rendered onto the display. It contains the most important drawing
/// settings along with grid positioning data. It also allows for conditional colouring and custom drawing.
///
/// For automatic menu layout cases, the layout is described in terms of font, color, position and justification using
/// this class based on some standard defaults.
/// @see CustomDrawingConfiguration
/// @see ConditionalColoring
public class ComponentSettings {
    public static final ComponentSettings NO_COMPONENT = new ComponentSettings(new NullConditionalColoring(),
            FONT_100_PERCENT, PortableAlignment.LEFT, new ComponentPositioning(0, 0), SHOW_NAME_VALUE,
            ControlType.TEXT_CONTROL, CustomDrawingConfiguration.NO_CUSTOM_DRAWING, false);

    private final FontInformation fontInfo;
    private final ConditionalColoring colors;
    private final PortableAlignment justification;
    private final ComponentPositioning position;
    private final ControlType controlType;
    private final RedrawingMode drawMode;
    private final CustomDrawingConfiguration customDrawing;
    private final boolean customised;

    public ComponentSettings(ConditionalColoring colors, FontInformation fontInfo, PortableAlignment justification,
                             ComponentPositioning position, RedrawingMode mode, ControlType controlType,
                             CustomDrawingConfiguration customDrawing, boolean custom)
    {
        this.fontInfo = fontInfo;
        this.colors = colors;
        this.justification = justification;
        this.position = position;
        this.drawMode = mode;
        this.customised = custom;
        this.controlType = controlType;
        this.customDrawing = customDrawing != null ? customDrawing : CustomDrawingConfiguration.NO_CUSTOM_DRAWING;
    }

    public CustomDrawingConfiguration getCustomDrawing() {
        return customDrawing;
    }

    /**
     * @return the font size for any text in the control
     */
    public FontInformation getFontInfo() {
        return fontInfo;
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