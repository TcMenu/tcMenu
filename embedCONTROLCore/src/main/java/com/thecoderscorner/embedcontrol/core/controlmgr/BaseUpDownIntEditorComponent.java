package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;

public abstract class BaseUpDownIntEditorComponent<T, W> extends BaseEditorComponent<W> {
    protected T currentVal;

    protected BaseUpDownIntEditorComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        onItemUpdated(controller.getMenuTree().getMenuState(item));
    }

    protected void bumpCount(int delta) {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;
        try {
            var correlation = componentControl.editorUpdatedItemDelta(item, delta);
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message", ex);
        }
    }

    @Override
    public String getControlText() {
        String str = "";
        if (controlTextIncludesName()) str = item.getName();
        if (!controlTextIncludesValue()) return str;

        str += " ";
        str += MenuItemFormatter.defaultInstance().formatForDisplay(item, currentVal);

        return str;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuState<?> newValue) {
        if (newValue != null && newValue.getValue() != null) {
            MenuState<T> state = (MenuState<T>) newValue;
            currentVal = state.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }
}