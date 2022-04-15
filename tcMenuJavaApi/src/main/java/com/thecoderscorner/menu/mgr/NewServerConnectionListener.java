package com.thecoderscorner.menu.mgr;

/**
 * when you implement this interface and pass that instance to start on a ServerConnectionManager then you'll receive
 * an event for each new connection created.
 */
public interface NewServerConnectionListener {
    void connectionCreated(ServerConnection connection);
}
