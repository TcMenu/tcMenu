package com.thecoderscorner.menu.remote.socket;

public interface SocketClientServerListener {
    void onConnectionCreated(SocketClientRemoteConnector connector);
    void onConnectionClosed(SocketClientRemoteConnector connector);
}
