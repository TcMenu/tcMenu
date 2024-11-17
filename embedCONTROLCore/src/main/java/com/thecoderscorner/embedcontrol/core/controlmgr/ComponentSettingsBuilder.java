package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.color.ConditionalColoring;
import com.thecoderscorner.embedcontrol.customization.FontInformation;
import com.thecoderscorner.embedcontrol.customization.customdraw.CustomDrawingConfiguration;
import com.thecoderscorner.menu.domain.*;

import java.util.Set;

import static com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent.PortableAlignment;
import static com.thecoderscorner.embedcontrol.customization.MenuFormItem.FONT_100_PERCENT;

/**
 * ComponentSettingsBuilder is a builder class for creating instances of ComponentSettings.
 * It provides various methods to customize the settings of a component, such as font, colors,
 * justification, position, control type, drawing mode, and custom drawing configuration.
 */
public class ComponentSettingsBuilder {
    public enum BuildingMode { MENU, TEXT, IMAGE }

    private final static Set<EditItemType> POSSIBLE_TIME_TYPES = Set.of(
            EditItemType.TIME_12H,
            EditItemType.TIME_24_HUNDREDS,
            EditItemType.TIME_24H,
            EditItemType.TIME_12H_HHMM,
            EditItemType.TIME_24H_HHMM);

    private BuildingMode mode;
    private String text;
    private MenuItem item;
    private FontInformation fontInfo = FONT_100_PERCENT;
    private ConditionalColoring colors;
    private PortableAlignment justification = PortableAlignment.LEFT_VAL_RIGHT;
    private ComponentPositioning position = new ComponentPositioning(0, 0);
    private ControlType controlType = ControlType.TEXT_CONTROL;
    private RedrawingMode drawMode = RedrawingMode.SHOW_NAME_VALUE;
    private CustomDrawingConfiguration customDrawing = CustomDrawingConfiguration.NO_CUSTOM_DRAWING;

    /// Create component settings builder object from a menu item. It defaults the fields to reasonable values as
    /// much as possible by setting the font to 100% size, setting the control type to the default, and setting the
    /// justification to the default too.
    /// @param item the menu item to build for
    /// @param color the colors to use for the control
    public static ComponentSettingsBuilder forMenuItem(MenuItem item, ConditionalColoring color) {
        var b = new ComponentSettingsBuilder();
        b.mode = BuildingMode.MENU;
        b.colors = color;
        b.item = item;
        b.withControlType(defaultControlForType(item));
        b.withJustification(defaultJustificationForType(b.controlType));
        return b;
    }

    /// Create a component settings builder object that will represent some text on the display at a
    /// particular grid position, will default to the text color, 100% font size, and left justification.
    /// @param text the text to present
    /// @param color the color set to use
    public static ComponentSettingsBuilder forText(String text, ConditionalColoring color) {
        var b = new ComponentSettingsBuilder();
        b.mode = BuildingMode.TEXT;
        b.colors = color;
        b.text = text;
        b.withControlType(ControlType.TEXT_CONTROL);
        b.withJustification(PortableAlignment.LEFT);
        return b;
    }

    private static PortableAlignment defaultJustificationForType(ControlType controlType) {
        return switch(controlType) {
            case HORIZONTAL_SLIDER, UP_DOWN_CONTROL -> PortableAlignment.LEFT_VAL_RIGHT;
            case BUTTON_CONTROL, VU_METER, ROTARY_METER -> PortableAlignment.CENTER;
            default -> PortableAlignment.LEFT;
        };
    }

    public static ControlType defaultControlForType(MenuItem item) {
        return switch(item) {
            case SubMenuItem _, BooleanMenuItem _, ActionMenuItem _ -> ControlType.BUTTON_CONTROL;
            case AnalogMenuItem _ -> ControlType.HORIZONTAL_SLIDER;
            case EnumMenuItem _, ScrollChoiceMenuItem _ -> ControlType.UP_DOWN_CONTROL;
            case Rgb32MenuItem _ -> ControlType.RGB_CONTROL;
            case RuntimeListMenuItem _ -> ControlType.LIST_CONTROL;
            case CustomBuilderMenuItem _ -> ControlType.AUTH_IOT_CONTROL;
            case EditableTextMenuItem txt when txt.getItemType() == EditItemType.GREGORIAN_DATE -> ControlType.DATE_CONTROL;
            case EditableTextMenuItem txt when POSSIBLE_TIME_TYPES.contains(txt.getItemType())  -> ControlType.TIME_CONTROL;
            default -> ControlType.TEXT_CONTROL;
        };
    }

    /// Override the font from the default 100% size to another value
    /// @param fontInfo  the font to override with.
    public ComponentSettingsBuilder withFont(FontInformation fontInfo) {
        this.fontInfo = fontInfo;
        return this;
    }

    /// Change the conditional coloring from the default one chosen.
    /// @param colors the conditional colors
    public ComponentSettingsBuilder withColors(ConditionalColoring colors) {
        this.colors = colors;
        return this;
    }

    /// Change the justification from the default value which is guessed during `forMenuItem` based on the control.
    /// @param justification the justification to use
    public ComponentSettingsBuilder withJustification(PortableAlignment justification) {
        this.justification = justification;
        return this;
    }

    /// Set the position of the control in the grid. Must always be set, for simpler cases with
    /// no span you can use `withRowCol`
    /// @param position the position and span in the grid to create with
    public ComponentSettingsBuilder withPosition(ComponentPositioning position) {
        this.position = position;
        return this;
    }

    /// Set the position of the control in the grid. Must always be set
    /// @param row the zero based row
    /// @param col the zero based column
    public ComponentSettingsBuilder withRowCol(int row, int col) {
        this.position = new ComponentPositioning(row, col);
        return this;
    }

    /// Override the control type that was guessed during `forMenuItem`. You should be careful that the control type
    /// you choose is compatible with the menu item type.
    /// @param controlType  the control type to use
    /// @throws IllegalArgumentException if the control type is invalid for the menu item
    public ComponentSettingsBuilder withControlType(ControlType controlType) {
        if(mode != BuildingMode.MENU) {
            controlType = ControlType.TEXT_CONTROL;
        } else if(!controlType.isSupportedFor(item)) {
            throw new IllegalArgumentException("Control type %s cannot render %s".formatted(controlType, item.getClass().getSimpleName()));
        } else {
            this.controlType = controlType;
        }
        return this;
    }

    /// Sets the drawing mode for the item, defaults to show name and item.
    /// @param drawMode  the drawing mode
    public ComponentSettingsBuilder withDrawMode(RedrawingMode drawMode) {
        this.drawMode = drawMode;
        return this;
    }

    /// Configure a custom drawing for the item, again make sure it is compatible with the menu type your using.
    /// @param customDrawing the custom drawing to be used, must be compatible with the menu item type.
    /// @throws IllegalArgumentException if the custom drawing is incompatible with the menu item type
    public ComponentSettingsBuilder withCustomDrawing(CustomDrawingConfiguration customDrawing) {
        if(!customDrawing.isSupportedFor(item)) {
            throw new IllegalArgumentException("Custom drawing %s cannot render %s".formatted(customDrawing, item.getClass().getSimpleName()));
        }
        this.customDrawing = customDrawing;
        return this;
    }

    /// Get the menuitem for this builder
    /// @return menu item
    public MenuItem getItem() {
        return item;
    }

    /// Get the static text associated with this builder
    /// @return  the static text
    public String getText() {
        return text;
    }

    /// Get the mode of the building, IE text, menu item etc.
    /// @return the building mode
    public BuildingMode getMode() {
        return mode;
    }

    /// Creates the component settings
    /// @return the built object
    public ComponentSettings build() {
        return new ComponentSettings(colors, fontInfo, justification, position, drawMode, controlType, customDrawing, true);
    }
}
