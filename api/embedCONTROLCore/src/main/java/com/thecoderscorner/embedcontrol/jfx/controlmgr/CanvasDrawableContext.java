package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.EditorComponent;
import com.thecoderscorner.menu.domain.MenuItem;

/**
 * Interface representing the context required for a drawable canvas component. It encapsulates
 * the necessary information needed to render a component within a canvas, including the menu
 * item, drawing settings, rendering status, and a general value.
 */
public interface CanvasDrawableContext {
    public MenuItem getItem();
    public ComponentSettings getDrawingSettings();
    public EditorComponent.RenderingStatus getStatus();
    Object getValue();
}
