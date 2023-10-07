package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.*;

import java.util.List;

/**
 * Represents the type of embedCONTROL control to use, these are hints to the rendering layer to help it to decide
 * what to display.
 */
public enum ControlType {
    /** a horizontal slider or progress style control that can present text in the middle */
    HORIZONTAL_SLIDER(AnalogMenuItem.class, FloatMenuItem.class),
    /** use up down buttons to control items have a range that can be moved through */
    UP_DOWN_CONTROL(AnalogMenuItem.class, ScrollChoiceMenuItem.class, EnumMenuItem.class),
    /** Use a text control for more or less any item, it shows the value in a label with an optional edit button */
    TEXT_CONTROL(MenuItem.class),
    /** Show the control as a button, in the case of boolean items, it will toggle */
    BUTTON_CONTROL(ActionMenuItem.class, SubMenuItem.class, BooleanMenuItem.class),
    /** Show the control as a VU meter style control for analog and float items only */
    VU_METER(AnalogMenuItem.class, FloatMenuItem.class),
    /** Show the control as a circular meter style control for analog and float items only */
    ROTARY_METER(AnalogMenuItem.class, FloatMenuItem.class),
    /** Show the control as a date that can be picked if editable */
    DATE_CONTROL(EditableTextMenuItem.class),
    /** Show the control as time, that can be edited if allowed */
    TIME_CONTROL(EditableTextMenuItem.class),
    /** Show the control as RGB, that can be edited if allowed using an RGB picker */
    RGB_CONTROL(Rgb32MenuItem.class),
    /** Show as a list of items */
    LIST_CONTROL(RuntimeListMenuItem.class),
    /** Show as the authorization and IoT control for that display technology */
    AUTH_IOT_CONTROL(CustomBuilderMenuItem.class),
    /** Indicates the component is not to be rendered */
    CANT_RENDER(MenuItem.class);

    private final List<Class<? extends MenuItem>> supportedTypes;

    ControlType(Class<? extends MenuItem>... menuItemTypes) {
        supportedTypes = List.of(menuItemTypes);
    }

    public boolean isSupportedFor(MenuItem item) {
        return supportedTypes.stream().anyMatch(ty -> ty.isAssignableFrom(item.getClass()));
    }
}
