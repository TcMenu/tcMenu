package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.MenuItem;

public interface CanvasDrawableContext {
    public MenuItem getItem();
    public ComponentSettings getDrawingSettings();
    public EditorComponent.RenderingStatus getStatus();
    Object getValue();
}
