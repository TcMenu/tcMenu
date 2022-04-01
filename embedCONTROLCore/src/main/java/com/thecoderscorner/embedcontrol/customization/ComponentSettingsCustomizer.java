package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.*;

public interface ComponentSettingsCustomizer extends ColorCustomizable {
    ComponentSettings getInitialSettings();
    void acceptSettingChange(int id, ComponentPositioning positioning, RedrawingMode drawingMode,
                             EditorComponent.PortableAlignment alignment, ControlType controlType, int fontSize) throws InvalidItemChangeException;
    void removeOverride(int id) throws InvalidItemChangeException;
}
