package com.thecoderscorner.embedcontrol.core.controlmgr;

import com.thecoderscorner.menu.domain.state.MenuState;
import com.thecoderscorner.menu.remote.commands.AckStatus;
import com.thecoderscorner.menu.remote.protocol.CorrelationId;

public interface EditorComponent<T> {
    enum PortableAlignment { LEFT, RIGHT, CENTER }
    enum RenderingStatus { NORMAL, RECENT_UPDATE, EDIT_IN_PROGRESS, CORRELATION_ERROR }

    void onItemUpdated(MenuState<?> newValue);
    void onCorrelation(CorrelationId correlationId, AckStatus status);
    void tick();
    T createComponent();

    void load(String data);
    String save();
}
