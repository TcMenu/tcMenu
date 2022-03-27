package com.thecoderscorner.embedcontrol.customization;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentPositioning;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;

@FunctionalInterface
public interface ComponentSettingsChangeConsumer {
    void acceptSettingChange(ComponentPositioning positioning, EditorComponent.RedrawingMode drawingMode,
                             EditorComponent.PortableAlignment alignment, int fontSize);
}
