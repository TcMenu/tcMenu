package com.thecoderscorner.menu.remote.socket;

import com.thecoderscorner.menu.remote.MenuCommandProtocol;
import com.thecoderscorner.menu.remote.StreamRemoteConnector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

public class SocketBasedConnector extends StreamRemoteConnector {
    private final String remoteHost;
    private final int remotePort;

    private final AtomicReference<SocketChannel> socketChannel = new AtomicReference<>();

    public SocketBasedConnector(ScheduledExecutorService executor, MenuCommandProtocol protocol, String remoteHost, int remotePort) {
        super(protocol, executor);
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void start() {
        executor.execute(this::threadReadLoop);
    }

    @Override
    public void stop() {
        executor.shutdownNow();
    }

    private void threadReadLoop() {
        logger.info("Starting socket read loop for {}:{}", remoteHost, remotePort);
        while(!Thread.currentThread().isInterrupted()) {
            try {
                if(attemptToConnect()) {
                    processMessagesOnConnection();
                }
                else {
                    Thread.sleep(5000);
                }
            }
            catch(InterruptedException e) {
                logger.info("Thread is interrupted, closing down");
                close();
                Thread.currentThread().interrupt();
            }
            catch(Exception ex) {
                logger.error("Exception on socket {}:{}", remoteHost, remotePort, ex);
                close();
            }
        }
        logger.info("Exiting socket read loop for {}:{}", remoteHost, remotePort);
    }

    private boolean attemptToConnect() throws IOException {
        if(socketChannel.get() == null || !socketChannel.get().isConnected()) {
            close();
            SocketChannel ch = SocketChannel.open();
            ch.socket().connect(new InetSocketAddress(remoteHost, remotePort), 10000);
            socketChannel.set(ch);
        }
        return true;
    }

    @Override
    protected void getAtLeastBytes(ByteBuffer inputBuffer, int len) throws IOException {
        SocketChannel sc = socketChannel.get();
        if(sc == null || !isConnected()) throw new IOException("Socket closed during read");
        do {
            inputBuffer.compact();
            int actual = sc.read(inputBuffer);
            inputBuffer.flip();
            if (actual <= 0) throw new IOException("Socket probably closed, read return was 0 or less");
        } while(inputBuffer.remaining()<len);
    }

    @Override
    protected void sendInternal(ByteBuffer outputBuffer) throws IOException {
        SocketChannel sc = socketChannel.get();
        while(isConnected() && sc != null && outputBuffer.hasRemaining()) {
            int len = sc.write(outputBuffer);
            if(len <= 0) {
                throw new IOException("Socket closed - returned 0 or less from write");
            }
        }
    }

    @Override
    public String getConnectionName() {
        return "TCP " + remoteHost + ":" + remotePort;
    }

    @Override
    public void close() {
        if(socketChannel.get() == null) return;

        try {
            socketChannel.get().close();
        } catch (IOException e) {
            logger.error("Unexpected error closing socket", e);
        }

        super.close();
        socketChannel.set(null);
    }
}
