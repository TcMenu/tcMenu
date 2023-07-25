package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;

/**
 * Provide the base capabilities that are needed for text editing, regardless of the UI technology used.
 * @param <T> the type of the underlying data
 * @param <W> the UI Node type.
 */
public abstract class BaseTextEditorComponent<T, W> extends BaseEditorComponent<W> {
    public T currentVal;

    protected BaseTextEditorComponent(MenuComponentControl controller, ComponentSettings settings, MenuItem item, ThreadMarshaller marshaller) {
        super(controller, settings, item, marshaller);
    }

    protected void validateAndSend(String text) {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;

        try {
            var toSend = MenuItemFormatter.defaultInstance().formatToWire(item, text);
            var correlation =  componentControl.editorUpdatedItem(item, toSend);
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message", ex);
            markRecentlyUpdated(RenderingStatus.CORRELATION_ERROR);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onItemUpdated(MenuItem item, MenuState<?> newValue) {
        this.item = item;
        if (newValue.getValue() != null)
        {
            MenuState<T> actualState = (MenuState<T>) newValue;
            currentVal = actualState.getValue();
            markRecentlyUpdated(RenderingStatus.RECENT_UPDATE);
        }
    }

    @Override
    public String getControlText() {
        String str = "";
        if (controlTextIncludesName())  str = MenuItemFormatter.defaultInstance().getItemName(item) + " ";
        if (controlTextIncludesValue()) str += MenuItemFormatter.defaultInstance().formatForDisplay(item, currentVal);
        return str;
    }

}