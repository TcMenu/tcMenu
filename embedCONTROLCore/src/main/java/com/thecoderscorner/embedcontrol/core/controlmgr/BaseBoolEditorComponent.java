package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;

/**
 * This is the core representation of a boolean editor component, it needs to be implemented by an actual UI layer.
 * It handles items updating and the textual content that is to be displayed, but leaves the drawing to the sub class.
 * @param <W> the window component type.
 */
public abstract class BaseBoolEditorComponent<W> extends BaseEditorComponent<W> {
    protected boolean currentVal = false;

    protected BaseBoolEditorComponent(MenuComponentControl controller, ComponentSettings settings,
                                      MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
    }

    @Override
    public void onItemUpdated(MenuState<?> newState) {
        if (newState.getValue() != null && newState.getValue() instanceof Boolean)
        {
            currentVal = (boolean) newState.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }

    @Override
    public String getControlText() {
        String prefix = "";
        if (controlTextIncludesName()) prefix = item.getName();

        if (!controlTextIncludesValue()) return prefix;

        return prefix + " " + MenuItemFormatter.defaultInstance().formatForDisplay(item, currentVal);
    }

    protected void toggleState() {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;

        try {
            var correlation =  componentControl.editorUpdatedItem(item, !currentVal);
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message", ex);
        }
    }
}
