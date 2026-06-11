package com.thecoderscorner.menu.remote.encryption;

public class AESEncryptionHandlerFactory implements EncryptionHandlerFactory {
    private final String key;

    public AESEncryptionHandlerFactory(String key) {
        this.key = key;
    }

    public ProtocolEncryptionHandler create() throws Exception {
        return new AESProtocolEncryptionHandler(key);
    }
}
