package com.thecoderscorner.menu.mgr;

public enum ServerConnectionMode {
    /** A connection that can be used only for pairing and nothing else */
    PAIRING,
    /** A connection that is not yet authenticated */
    UNAUTHENTICATED,
    /** A fully authenticated connection */
    AUTHENTICATED,
    /** The connection with the remote has been lost */
    DISCONNECTED
}
