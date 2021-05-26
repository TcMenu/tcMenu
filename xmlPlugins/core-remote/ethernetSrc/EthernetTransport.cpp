/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#include "EthernetTransport.h"
#include <TaskManager.h>

using namespace tcremote;

#if ETHERNET_BUFFER_SIZE > 0 // we need buffering when dealing with Ethernet2

bool EthernetTagValTransport::available() {
	return client && client.connected();
}

bool EthernetTagValTransport::connected() {
	return client && client.connected();
}

int EthernetTagValTransport::writeChar(char data) {
    // only uncomment below for worst case debugging..
//	serdebug2("writing ", data);
    if(bufferPosition >= sizeof(bufferData)) {
        flush();
        // if the buffer isn't empty, something went wrong.
        if(bufferPosition != 0) return 0;
    }

	
    // and lastly add the byte
    return bufferData[bufferPosition++] = data;
}

int EthernetTagValTransport::writeStr(const char* data) {
    // only uncomment below for worst case debugging..
//	serdebug2("writing ", data);
    int len = strlen(data);
	for(int i = 0; i < len; ++i) {
        if(writeChar(data[i]) == 0) {
            return 0;
        }
    }
    return len;
}

void EthernetTagValTransport::flush() {
	if(!client || bufferPosition == 0) return;

    if(client.write(bufferData, bufferPosition) == bufferPosition) {
        serdebugF2("Buffer written ", bufferPosition);
        bufferPosition = 0;
    }
    else {
        serdebugF2("Buffer write fail ", bufferPosition);
    }
    client.flush();
}

uint8_t EthernetTagValTransport::readByte() {
	return client.read();
}

bool EthernetTagValTransport::readAvailable() {
	return client && client.connected() && client.available();
}

#else // unbuffed client for all fully implemented stacks

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

#endif

int fromWiFiRSSITo4StateIndicator(int strength) {
    int qualityIcon = 0;
    if(strength > -50) qualityIcon = 4;
    else if(strength > -60) qualityIcon = 3;
    else if(strength > -75) qualityIcon = 2;
    else if(strength > -90) qualityIcon = 1;
    return qualityIcon;
}

bool tcremote::EthernetInitialisation::attemptInitialisation() {
    serdebugF("Initialising server ");
    this->server->begin();
    initialised = true;
    return initialised;
}

bool tcremote::EthernetInitialisation::attemptNewConnection(TagValueTransport *transport) {
    EthernetClient client = server->available();
    if(client) {
        serdebugF("Client found");
        reinterpret_cast<EthernetTagValTransport*>(transport)->setClient(client);
        return true;
    }
    return false;
}
