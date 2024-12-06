package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.MenuComponentControl;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;

/**
 * IntegerUpDownEditorComponent is a concrete implementation of the
 * UpDownEditorComponentBase class that specifically handles Integer values.
 * It provides the basic UI components required for editing integer menu items
 * with increment and decrement buttons.
 */
public class IntegerUpDownEditorComponent extends UpDownEditorComponentBase<Integer> {
    public IntegerUpDownEditorComponent(MenuItem item, MenuComponentControl remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(item, remote, settings, marshaller);
    }
}