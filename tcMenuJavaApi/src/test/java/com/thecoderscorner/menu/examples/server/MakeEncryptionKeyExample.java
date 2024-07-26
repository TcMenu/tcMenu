package com.thecoderscorner.menu.examples.server;

import com.thecoderscorner.menu.remote.encryption.AESEncryptionHandlerFactory;
import com.thecoderscorner.menu.remote.encryption.AESProtocolEncryptionHandler;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Base64;

/**
 * This example shows how to make an encryption key pair, use them, serialise them, and create a tcMenuApi protocol
 * encryption handler from it too.
 */
public class MakeEncryptionKeyExample {
    public static void main(String[] args) throws Exception {
        // first we generate a key and iv that can be used both sides, you can also use TcMenu Designer to do this.
        var key = AESProtocolEncryptionHandler.generateAesKey(256);
        var iv = AESProtocolEncryptionHandler.generateIv();

        // this is how you get the key data as a string.
        String encodedKey = Base64.getEncoder().encodeToString(key.getEncoded());
        String encodedIv = Base64.getEncoder().encodeToString(iv.getIV());

        // print out what we've got.
        System.out.println("Algorithm of key: " + key.getAlgorithm());
        System.out.println("Encoded key format: " + encodedKey);
        System.out.println("Encoded iv format: " + encodedIv);

        // here we convert the string representations back into objects.
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        byte[] decodedIv = Base64.getDecoder().decode(encodedIv);
        var keyReadBack = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        var ivReadBack = new IvParameterSpec(decodedIv);

        //print out what we got from the read back
        System.out.println("Algorithm of key: " + keyReadBack.getAlgorithm());
        System.out.println("Read back key: " + Arrays.toString(keyReadBack.getEncoded()));
        System.out.println("Iv read back: " + Arrays.toString(ivReadBack.getIV()));

        System.out.println();
        System.out.println("Key data for C++");
        System.out.println();

        System.out.print("Key as C++ array: byte keyData[] = {");
        boolean needSep = false;
        for (byte keyByte : keyReadBack.getEncoded()) {
            System.out.printf("%s0x%02x", needSep ? ", ": " ", keyByte);
            needSep = true;
        }
        System.out.println("};");

        needSep = false;
        System.out.print("IV as C++ array: byte ivData[] = {");
        for (byte keyByte : ivReadBack.getIV()) {
            System.out.printf("%s%02x", needSep ? ", ": " ", keyByte);
            needSep = true;
        }
        System.out.println("};");
        System.out.println();



        // create a protocol encryption handler
        var encryptionHandler = new AESEncryptionHandlerFactory(encodedKey).create();

        System.out.println("start " + LocalDateTime.now());
        for(int i=0;i<2000;i++) {

            // encrypt an important message
            ByteBuffer bb = ByteBuffer.allocate(256);
            String s = "hello world " + i;
            bb.put(s.getBytes()).flip();
            byte[] cypherText = encryptionHandler.encryptBuffer(bb);

            // now clear out the byte buffer and use it to decrypt the data.
            bb.clear();
            bb.put(cypherText).flip();
            byte[] plainText = encryptionHandler.decryptBuffer(bb, cypherText.length);

            plainText[0] = 0;
//            System.out.println(new String(plainText));
        }
        System.out.println("end " + LocalDateTime.now());

    }
}
