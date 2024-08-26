/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#include "ClientEthernetTransport.h"
#include <TaskManager.h>

using namespace tcremote;

#if ETHERNET_BUFFER_SIZE > 0 // we need buffering when dealing with Ethernet2

bool ClientEthernetTagValTransport::available() {
	return client && client.connected();
}

bool ClientEthernetTagValTransport::connected() {
	return client && client.connected();
}

void ClientEthernetTagValTransport::flush() {
    if(!client || writeBufferPos == 0) return;

    if((int)client.write(writeBuffer, writeBufferPos) == writeBufferPos) {
        serdebugF2("Buffer written ", writeBufferPos);
        writeBufferPos = 0;
        client.flush();
    }
    else {
        writeBufferPos = 0;
        close();
    }
}

int ClientEthernetTagValTransport::fillReadBuffer(uint8_t* dataBuffer, int maxData) {
    if(client && client.connected() && client.available()) {
        auto amt = client.read(dataBuffer, maxData);
        if(amt <= 0) {
            close();
            return 0;
        }
        serdebugF2("read to buffer ", amt);
        return amt;
    }
    return 0;
}

void ClientEthernetTagValTransport::close() {
    serdebugF("socket close");
    BaseBufferedRemoteTransport::close();
    client.stop();
}

#else // unbuffed client - requires library to support Nagle algorythm.

bool EthernetTagValTransport::available() {
	return client && client.connected();
}

bool EthernetTagValTransport::connected() {
	return client && client.connected();
}

int EthernetTagValTransport::writeChar(char data) {
    // only uncomment below for worst case debugging..
//	serdebug2("writing ", data);
	return client.write(data);
}

int EthernetTagValTransport::writeStr(const char* data) {
    // only uncomment below for worst case debugging..
//	serdebug2("writing ", data);
	return client.write(data);
}

void EthernetTagValTransport::flush() {
	if(client) client.flush();
}

uint8_t EthernetTagValTransport::readByte() {
	return client.read();
}

bool EthernetTagValTransport::readAvailable() {
	return client && client.connected() && client.available();
}

void EthernetTagValTransport::close() {
    serdebugF("socket close");
    client.stop();
    currentField.msgType = UNKNOWN_MSG_TYPE;
    currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
}

#endif

bool ClientEthernetInitialisation::attemptInitialisation() {
#ifdef ARDUINO_ARCH_STM32
    // we'll keep checking if the link is up before trying to initialise further
    if(Ethernet.linkStatus() == LinkOFF) return false;
#endif
    serdebugF("Initialising server ");
    initialised = true;
    return initialised;
}

bool ClientEthernetInitialisation::attemptNewConnection(BaseRemoteServerConnection *remoteServerConnection) {
    if(!checkBackoffPeriod()) return false;

    client.connect(host, port);
    if(client.connected()) {
        serdebugF("Client found");
        auto* tvCon = reinterpret_cast<TagValueRemoteServerConnection*>(remoteServerConnection);
        reinterpret_cast<ClientEthernetTagValTransport*>(tvCon->transport())->setClient(client);
        return true;
    }
    return false;
}

bool ClientEthernetInitialisation::checkBackoffPeriod() {
    if(backOffPeriod == 0) {
        backOffPeriod = DEFAULT_BACKOFF_PERIOD;
        return true;
    }
    backOffPeriod--;
    return false;
}
