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
    if(socket){
        socket->close();
        delete socket;
    }
}

void MBedEthernetTransport::flush() {
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
    if(readPos >= sizeof(readBuf)) return -1;
    return readBuf[readPos++];
}

bool MBedEthernetTransport::readAvailable() {
    if(readPos >= lastReadAmt) {
        int amt = socket->recv(readBuf, sizeof(readBuf));
        if(amt == NSAPI_ERROR_WOULD_BLOCK) return false;
        if(amt > 0) {
            readPos = 0;
            lastReadAmt = amt;
        }
        else {
            close();
            return false;
        }
    }
    return readPos < sizeof(readBuf);
}

bool MBedEthernetTransport::available() {
    if(readPos >= sizeof(writeBuf)) {
        flush();
    }
    return (readPos < sizeof(writeBuf));
}

bool MBedEthernetTransport::connected() {
    return isOpen;
}

void MBedEthernetTransport::close() {
    flush();
    isOpen = false;
    socket->close();
}

void MBedEthernetTransport::endMsg() {
    TagValueTransport::endMsg();
    flush();
}

// ----------------- Ethernet Remote Server --------------

EthernetTagValServer remoteServer = EthernetTagValServer();

void EthernetTagValServer::begin(int bindingPort, const ConnectorLocalInfo* localInfo) {
    if(defNetwork == NULL) {
        serdebugF("No network interface found, not initialising network");
        return;
    }
    listenPort = bindingPort;
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

        serdebugF2("Connected to network on IP ", defNetwork->get_ip_address());
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

        taskManager.scheduleFixedRate(WRITE_DELAY, this, TIME_MILLIS);
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