package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.CurrentScrollPosition;
import com.thecoderscorner.menu.domain.util.MenuItemFormatter;

/**
 * A UI component that leverages the base functionality provided by UpDownEditorComponentBase to create
 * an editor for managing scrolling through items. This component is specifically designed to handle
 * current scroll position updates for a menu item.
 */
public class ScrollUpDownEditorComponent extends UpDownEditorComponentBase<CurrentScrollPosition> {
    public ScrollUpDownEditorComponent(MenuItem item, MenuComponentControl remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(item, remote, settings, marshaller);
    }

    @Override
    public String getControlText() {
        var text = "";
        if(controlTextIncludesName()) text += MenuItemFormatter.defaultInstance().getItemName(item);
        if(controlTextIncludesValue()) text += " " + currentVal.getValue();
        return text;
    }

    @Override
    protected void bumpCount(int delta) {
        if (status == RenderingStatus.EDIT_IN_PROGRESS) return;
        try {
            var posNow = currentVal.getPosition();
            var csp = new CurrentScrollPosition(posNow + delta, "");
            var correlation = componentControl.editorUpdatedItem(item, csp.toString());
            editStarted(correlation);
        } catch (Exception ex) {
            logger.log(System.Logger.Level.ERROR, "Failed to send message");
        }
    }
}
