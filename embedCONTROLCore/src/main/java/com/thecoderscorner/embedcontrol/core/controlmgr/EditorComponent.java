package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

public interface EditorComponent<T> {
    enum PortableAlignment { LEFT, RIGHT, CENTER }
    enum RenderingStatus { NORMAL, RECENT_UPDATE, EDIT_IN_PROGRESS, CORRELATION_ERROR }
    enum RedrawingMode { SHOW_NAME, SHOW_VALUE, SHOW_NAME_IN_LABEL, SHOW_NAME_VALUE, SHOW_LABEL_NAME_VALUE }

    void onItemUpdated(MenuState<?> newValue);
    void onCorrelation(CorrelationId correlationId, AckStatus status);
    void tick();
    T createComponent();

    void load(String data);
    String save();
}
