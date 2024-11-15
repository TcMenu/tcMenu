package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;

/// BaseUpDownIntEditorComponent is an abstract class that handles
/// the core logic for an editor component with up/down integer adjustments.
/// This class extends [BaseEditorComponent] and encapsulates the functionalities
/// required to manage integer values.
///
/// @param <T> The type of the value this editor component will manage.
/// @param <W> The type of the widget component.
public abstract class BaseUpDownIntEditorComponent<T, W> extends BaseEditorComponent<W> {
    protected T currentVal;

    protected BaseUpDownIntEditorComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
        onItemUpdated(item, controller.getMenuTree().getMenuState(item));
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
        if (controlTextIncludesName()) str = MenuItemFormatter.defaultInstance().getItemName(item);
        if (!controlTextIncludesValue()) return str;

        str += " ";
        str += MenuItemFormatter.defaultInstance().formatForDisplay(item, currentVal);

        return str;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuItem item, MenuState<?> newValue) {
        this.item = item;
        if (newValue != null && newValue.getValue() != null) {
            MenuState<T> state = (MenuState<T>) newValue;
            currentVal = state.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }
}