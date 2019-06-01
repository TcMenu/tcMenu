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

EthernetTagValServer remoteServer = EthernetTagValServer();

EthernetTagValTransport::EthernetTagValTransport() {
}

EthernetTagValTransport::~EthernetTagValTransport() {
}

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

EthernetTagValServer::EthernetTagValServer() : messageProcessor(msgHandlers, MSG_HANDLERS_SIZE) {
	this->server = NULL;
}

void EthernetTagValServer::begin(EthernetServer* server, const ConnectorLocalInfo* localInfo) {
    serdebugFHex("Initialising server ", (unsigned int)server);
	this->server = server;
	this->server->begin();
    serdebugF("Initialising connector");
	this->connector.initialise(&transport, &messageProcessor, localInfo);
	taskManager.scheduleFixedRate(TICK_INTERVAL, this, TIME_MILLIS);
}

void EthernetTagValTransport::close() {
	currentField.msgType = UNKNOWN_MSG_TYPE;
	currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
	client.stop();
}

void EthernetTagValServer::exec() {
    connector.tick();

    if(!transport.connected()) {
		EthernetClient client = server->available();
		if(client) {
            serdebugF("Client found");
			transport.setClient(client);
		}
	}
}

