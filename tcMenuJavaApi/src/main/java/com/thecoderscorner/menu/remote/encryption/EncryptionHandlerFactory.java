package com.thecoderscorner.menu.remote.encryption;

public interface EncryptionHandlerFactory {
    ProtocolEncryptionHandler create() throws Exception;
}
