package com.thecoderscorner.embedcontrol.core.creators;

import java.util.UUID;

public interface RemotePanelDisplayable {
    ConnectionCreator getCreator();
    UUID getUuid();
    String getPanelName();
}
