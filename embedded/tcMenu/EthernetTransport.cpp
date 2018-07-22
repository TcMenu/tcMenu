/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

#include <EthernetTransport.h>
#include <TaskManager.h>

EthernetTagValServer ethTagValServer = EthernetTagValServer();

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
	serdebug2("writing ", data);
	return client.write(data);
}

int EthernetTagValTransport::writeStr(const char* data) {
	serdebug2("writing ", data);
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

EthernetTagValServer::EthernetTagValServer() : connector(&transport, 0) {
	this->server = NULL;
}

void EthernetTagValServer::begin(EthernetServer* server, const char* namePgm) {
	this->server = server;
	this->connector.setName(namePgm);
	taskManager.scheduleFixedRate(TICK_INTERVAL, []{ethTagValServer.runLoop();}, TIME_MILLIS);
}

void EthernetTagValTransport::close() {
	currentField.msgType = UNKNOWN_MSG_TYPE;
	currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
	client.stop();
}

void EthernetTagValServer::runLoop() {
	if(transport.connected()) {
		connector.tick();
	}
	else {
		EthernetClient client = server->available();
		if(client) {
			transport.setClient(client);
		}
	}
}

