package com.thecoderscorner.embedcontrol.jfx.controlmgr;

import com.thecoderscorner.embedcontrol.core.controlmgr.ComponentSettings;
import com.thecoderscorner.embedcontrol.core.controlmgr.ThreadMarshaller;
import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.remote.RemoteMenuController;

public class IntegerUpDownEditorComponent extends UpDownEditorComponentBase<Integer> {
    public IntegerUpDownEditorComponent(MenuItem item, RemoteMenuController remote, ComponentSettings settings, ThreadMarshaller marshaller) {
        super(item, remote, settings, marshaller);
    }
}