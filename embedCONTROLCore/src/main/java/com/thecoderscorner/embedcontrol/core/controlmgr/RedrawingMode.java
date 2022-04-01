package com.thecoderscorner.embedcontrol.core.controlmgr;

/**
 * How the item should be rendered onto the display, value, value and name or name only, or even hidden from display
 */
public enum RedrawingMode {
    /** show the name only with no value */
    SHOW_NAME,
    /** Show the value only with no name */
    SHOW_VALUE,
    /** show the name and value */
    SHOW_NAME_VALUE,
    /** Hide this item */
    HIDDEN
}
