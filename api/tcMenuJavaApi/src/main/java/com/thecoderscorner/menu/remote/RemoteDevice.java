package com.thecoderscorner.menu.remote;

/**
 * This describes a remote connection at its most basic level, just the user that is connected and the name of the
 * connection, no other operations are supported at this level, but it provides a means of dealing with any type of
 * remote connection, be it acting as a server or acting as a client.
 */
public interface RemoteDevice {
    String getUserName();
    String getConnectionName();
}
