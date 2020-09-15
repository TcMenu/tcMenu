/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */


#include "MBedEthernetTransport.h"

MBedEthernetTransport::~MBedEthernetTransport() {
    if(socket) {
        socket->close();
    }
}

void MBedEthernetTransport::flush() {
    if(writePos == 0 || socket == nullptr) return;

    int written = socket->send(writeBuf, writePos);
    if(written == NSAPI_ERROR_WOULD_BLOCK) return;
    if(written > 0) {
        int left = writePos - written;
        if(left > 0) {
            memmove(writeBuf, &writeBuf[written], left);
            writePos = left;
        }
        else writePos = 0;
    }
    else {
        close();
    }
}

int MBedEthernetTransport::writeChar(char data) {
    if(writePos >= sizeof(writeBuf)) {
        flush();
    }
    if(writePos < sizeof(writeBuf)) {
        writeBuf[writePos] = data;
        writePos++;
        return 1;
    }
    else return 0;
}

int MBedEthernetTransport::writeStr(const char *data) {
    int len = strlen(data);
    for(int i=0; i<len; i++) {
        if(writeChar(data[i]) == 0) return 0;
    }
    return len;
}

uint8_t MBedEthernetTransport::readByte() {
    return (readAvailable()) ? readBuf[readPos++] : -1;
}

bool MBedEthernetTransport::readAvailable() {
    if(socket == nullptr) return false;

    if(readPos >= lastReadAmt) {
        int amt = socket->recv(readBuf, sizeof(readBuf));
        if(amt == NSAPI_ERROR_WOULD_BLOCK) return false;
        if(amt > 0) {
            readPos = 0;
            lastReadAmt = amt;
            return true;
        }
        else {
            close();
            lastReadAmt =0;
            readPos = 0;
            return false;
        }
    } else return true;
}

bool MBedEthernetTransport::available() {
    if(socket == nullptr) return false;

    if(readPos >= sizeof(writeBuf)) {
        flush();
    }
    return (readPos < sizeof(writeBuf));
}

void MBedEthernetTransport::close() {
    if(socket == nullptr) return;

    flush();
    isOpen = false;
    socket->close();
    // socket is now a dangling pointer and must be cleared
    socket = nullptr;
}

void MBedEthernetTransport::endMsg() {
    TagValueTransport::endMsg();
    flush();
}

// ----------------- Ethernet Remote Server --------------

EthernetTagValServer remoteServer;

void EthernetTagValServer::begin(int bindingPort, const ConnectorLocalInfo* localInfo) {
    if(defNetwork == NULL) {
        serdebugF("No network interface found, not initialising network");
        return;
    }
    listenPort = bindingPort;

    connector.initialise(&transport, &messageProcessor, localInfo);

    defNetwork->set_blocking(false);
    server.set_blocking(false);

    // this is the message ticker
    taskManager.scheduleOnce(TICK_INTERVAL, this, TIME_MILLIS);
    // this does some very basic caching of messages to try and group them up on the wire
}

void EthernetTagValServer::exec() {
    if(!boundToAddr) {
        if(defNetwork->connect() != NSAPI_ERROR_IS_CONNECTED) {
            taskManager.scheduleOnce(250, this);
            return;
        }

        SocketAddress localAddr;
        defNetwork->get_ip_address(&localAddr);
        serdebugF2("Connected to network on IP ", localAddr.get_ip_address());
        if(server.open(defNetwork) != 0) {
            serdebugF("Could not open socket");
            taskManager.scheduleOnce(1,this, TIME_SECONDS);
            return;
        }
        if(server.bind(listenPort) != 0 || server.listen(1) != 0) {
            serdebugF2("Could not bind to ", listenPort);
            taskManager.scheduleOnce(1, this, TIME_SECONDS);
            return;
        }
        boundToAddr = true;

        taskManager.scheduleFixedRate(TICK_INTERVAL, this, TIME_MILLIS);
        //secondly we provide a low speed writer task that just flushes the buffer a five times a second.
        taskManager.scheduleFixedRate(WRITE_DELAY, &transport, TIME_MILLIS);

        serdebugF2("Listen fully bound to ", listenPort);

        return;
    }
    if(!transport.connected()) {
        nsapi_error_t acceptErr;
        auto tcpSock = server.accept(&acceptErr);
        if(acceptErr == NSAPI_ERROR_OK) {
            serdebugF("Client found");
            transport.setSocket(tcpSock);
        }
        else if(acceptErr != NSAPI_ERROR_WOULD_BLOCK) {
            serdebugF2("Error code ", acceptErr);
        }
    }
    else {
        connector.tick();
    }
}