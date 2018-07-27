/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * SerialTransport.cpp - the serial wire transport for a TagValueConnector.
 */

#include "SerialTransport.h"
#include "tcMenu.h"

extern const char applicationName[];

SerialTagValServer serialServer;

SerialTagValueTransport::SerialTagValueTransport() : TagValueTransport() {
	this->serialPort = NULL;
}

void SerialTagValueTransport::close() {
	currentField.msgType = UNKNOWN_MSG_TYPE;
	currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
}

void SerialTagValServer::begin(Stream* portStream, const char* namePgm) {
	serPort.setStream(portStream);
	connector.setName(namePgm);
	taskManager.scheduleFixedRate(TICK_INTERVAL, []{serialServer.runLoop();}, TIME_MILLIS);
}

SerialTagValServer::SerialTagValServer() : connector(&serPort, 0) {
}

void SerialTagValServer::runLoop() {
	connector.tick();
}
