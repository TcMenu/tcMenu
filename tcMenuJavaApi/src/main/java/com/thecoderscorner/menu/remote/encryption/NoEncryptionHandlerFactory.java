package com.thecoderscorner.menu.remote.encryption;

public class NoEncryptionHandlerFactory implements EncryptionHandlerFactory {
    public ProtocolEncryptionHandler create() throws Exception {
        return null;
    }
}
