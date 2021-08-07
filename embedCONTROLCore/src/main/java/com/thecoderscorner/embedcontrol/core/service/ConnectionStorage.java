package com.thecoderscorner.embedcontrol.core.service;

import com.thecoderscorner.embedcontrol.core.creators.RemotePanelDisplayable;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface ConnectionStorage<T extends RemotePanelDisplayable> {
    List<T> loadAllRemoteConnections() throws IOException;
    void savePanel(T panel);
    boolean deletePanel(UUID id);
}
