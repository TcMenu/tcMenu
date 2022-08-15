package com.thecoderscorner.menu.driver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * This class serves as a helper that works alongside the native driver test sketch that we use to test device network
 * drivers. You normally start the sketch first, and then this in order to run the network layer through a few basic
 * tests.
 */
public class NativeDriverJavaClient {
    private final String HOST_IP = "192.168.0.202";
    private final int HOST_PORT = 3333;

    public NativeDriverJavaClient() {

    }

    public synchronized void logIt(String line) {
        System.out.print(new Date());
        System.out.print(' ');
        System.out.println(line);
    }

    public void runTheTest() {
        logIt("Starting socket connect on " + HOST_IP + ":" + HOST_PORT);
        try(var socket = new Socket(HOST_IP, HOST_PORT)) {
            var socketClosed = new AtomicBoolean(false);
            if(!socket.isConnected()) throw new IOException("Didn't connect");

            logIt("Connected to host");

            Thread th = new Thread(() -> {
                try {
                    logIt("Start read thread");
                    byte[] readBuffer = new byte[255];
                    while (!Thread.interrupted() && !socketClosed.get()) {
                        int actual = socket.getInputStream().read(readBuffer);
                        logIt("Read bytes " + new String(readBuffer));
                        if (actual <= 0) {
                            logIt("Socket closed in read");
                            socketClosed.set(true);
                        }
                    }
                }catch(Exception e) {
                    socketClosed.set(true);
                    e.printStackTrace();
                } finally {
                    logIt("End read thread");
                }
            });
            th.start();

            while(!socketClosed.get()) {
                byte[] outBuffer = new byte[100];
                for(int i=0;i<100;i++) {
                    outBuffer[i] = (byte)i;
                }

                socket.getOutputStream().write(outBuffer);
                logIt("Written bytes " + new String(outBuffer));
                Thread.sleep(10000);
            }
            th.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        var testCase = new NativeDriverJavaClient();
        testCase.runTheTest();
    }
}
