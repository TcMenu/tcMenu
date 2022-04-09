package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.*;

import java.util.List;

public enum ControlType {
    HORIZONTAL_SLIDER(AnalogMenuItem.class),
    UP_DOWN_CONTROL(AnalogMenuItem.class, ScrollChoiceMenuItem.class, EnumMenuItem.class),
    TEXT_CONTROL(MenuItem.class),
    BUTTON_CONTROL(ActionMenuItem.class, SubMenuItem.class, BooleanMenuItem.class),
    VU_METER(AnalogMenuItem.class),
    DATE_CONTROL(EditableTextMenuItem.class),
    TIME_CONTROL(EditableTextMenuItem.class),
    RGB_CONTROL(Rgb32MenuItem.class),
    LIST_CONTROL(RuntimeListMenuItem.class),
    AUTH_IOT_CONTROL(CustomBuilderMenuItem.class),
    CANT_RENDER(MenuItem.class);

    private final List<Class<? extends MenuItem>> supportedTypes;

    ControlType(Class<? extends MenuItem>... menuItemTypes) {
        supportedTypes = List.of(menuItemTypes);
    }

    boolean isSupportedFor(MenuItem item) {
        return supportedTypes.stream().anyMatch(ty -> ty.isAssignableFrom(item.getClass()));
    }
}
