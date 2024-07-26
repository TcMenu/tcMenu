package com.thecoderscorner.menu.remote.encryption;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A protocol encryption class that can encrypt and decrypt message data being sent on a TagVal protocol channel using
 * an algorithm that is supported by both device and API. The input takes a byte buffer and that contains a raw encrypted
 * payload and coverts it into data.
 */
public interface ProtocolEncryptionHandler {
    /**
     * Low level function to encrypt the bytes in an input buffer
     * @param inputBuffer the buffer to encrypt
     * @return an encrypted buffer with the total length in the first two bytes
     * @throws IOException if there is a problem
     */
    byte[] encryptBuffer(ByteBuffer inputBuffer) throws IOException;

    /**
     * Low level function to decrypt the bytes in an input buffer up to len, it is assumed that input buffer has at
     * least len bytes remaining. If it does not it may fail
     * @param inputBuffer the buffer to decrypt
     * @param len the length of data to process
     * @return the decrypted data
     * @throws IOException if there is a problem
     */
    byte[] decryptBuffer(ByteBuffer inputBuffer, int len) throws IOException;

    /**
     * Returns a byte buffer that decryption can take place into, usually this requires an extra buffer.
     * @return a ready allocated buffer
     */
    ByteBuffer getDecryptBuffer();
}
