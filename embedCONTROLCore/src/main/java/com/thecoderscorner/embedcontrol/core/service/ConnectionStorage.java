package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;
import com.thecoderscorner.embedcontrol.customization.ScreenLayoutPersistence;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ConnectionStorage<T extends ScreenLayoutPersistence> {
    List<T> loadAllRemoteConnections() throws IOException;
    void savePanel(T panel);
    boolean deletePanel(UUID id);
}
