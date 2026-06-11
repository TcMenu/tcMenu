package com.thecoderscorner.menu.remote.encryption;

import com.thecoderscorner.menu.remote.SharedStreamConnection;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * This implements encryption and decryption based on AES CBC mode with an initialisation vector.
 */
public class AESProtocolEncryptionHandler implements ProtocolEncryptionHandler {

    private final static String AES_ALGO_NAME = "AES/CBC/PKCS5Padding";
    private static final SecureRandom secureRandom = new SecureRandom();
    private final SecretKey configuredKey;
    private final Cipher encryptCipher;
    private final Cipher decryptCipher;
    private final ByteBuffer decryptBuffer = ByteBuffer.allocate(SharedStreamConnection.MAX_MSG_EXPECTED).order(ByteOrder.BIG_ENDIAN).flip();

    public AESProtocolEncryptionHandler(SecretKey configuredKey) throws Exception {
        this.configuredKey = configuredKey;
        encryptCipher = Cipher.getInstance(AES_ALGO_NAME);
        decryptCipher = Cipher.getInstance(AES_ALGO_NAME);
        encryptCipher.init(Cipher.ENCRYPT_MODE, configuredKey, generateIv());
    }

    public AESProtocolEncryptionHandler(String key) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        configuredKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        encryptCipher = Cipher.getInstance(AES_ALGO_NAME);
        decryptCipher = Cipher.getInstance(AES_ALGO_NAME);
    }

    public static SecretKey generateAesKey(int bitsNeeded) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(bitsNeeded);
        return keyGenerator.generateKey();
    }

    public static IvParameterSpec generateIv() {
        var iv = new byte[16];
        secureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
    }

    @Override
    public byte[] encryptBuffer(ByteBuffer inputBuffer) throws IOException {
        try {
            IvParameterSpec ivParameterSpec = generateIv();
            encryptCipher.init(Cipher.ENCRYPT_MODE, configuredKey, ivParameterSpec);

            // we need to work in multiples of 16, work out the next largest
            var in = inputBuffer.remaining();
            var remainder = in % 16;
            var totalSize = remainder == 0 ? in : (in + (16 - remainder));
            // copy data into byte array
            byte[] data = new byte[totalSize];
            inputBuffer.get(data, 0, inputBuffer.remaining());
            // pad out end of buffer with 0s
            for(int i=in;i<totalSize;i++) data[i] = 0;
            // do the encryption and also provide the IV to the other side
            byte[] encrypted = new byte[encryptCipher.getOutputSize(totalSize) + 16];
            System.arraycopy(ivParameterSpec.getIV(), 0, encrypted, 0, 16);
            encryptCipher.doFinal(data, 0, totalSize, encrypted, 16);
            return encrypted;
        } catch (Exception e) {
            throw new IOException("Encrypt failed", e);
        }
    }

    @Override
    public byte[] decryptBuffer(ByteBuffer inputBuffer, int len) throws IOException {
        try {
            byte[] iv = new byte[16];
            inputBuffer.get(iv);
            byte[] encryptedData = new byte[len-16];
            decryptCipher.init(Cipher.DECRYPT_MODE, configuredKey, new IvParameterSpec(iv));
            inputBuffer.get(encryptedData);
            return decryptCipher.doFinal(encryptedData);
        } catch(Exception ex) {
            throw new IOException("Decrypt failed", ex);
        }
    }

    @Override
    public ByteBuffer getDecryptBuffer() {
        return decryptBuffer;
    }
}
