package com.thecoderscorner.embedcontrol.core.controlmgr;

/// How the text of the item should be rendered onto the display, it is interpreted by the [MenuEditorFactory] in
/// order to present the item as intended.
public enum RedrawingMode {
    /// show the name only with no value
    SHOW_NAME,
    /// Show the value only with no name
    SHOW_VALUE,
    /// show the name and value
    SHOW_NAME_VALUE,
    /// Hide this item completely
    HIDDEN
}
