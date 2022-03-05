package com.thecoderscorner.menu.mgr;

import java.util.List;

public interface ServerConnectionManager {
    List<ServerConnection> getServerConnections();
    void start(NewServerConnectionListener listener);
    void stop() throws Exception;
}
