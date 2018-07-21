/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * SerialTransport.cpp - the serial wire transport for a TagValueConnector.
 */

#include "SerialTransport.h"
#include "tcMenu.h"

SerialTagValueTransport::SerialTagValueTransport(Stream* serialPort) : TagValueTransport() {
	this->serialPort = serialPort;
}

void SerialTagValueTransport::endMsg() {
	TagValueTransport::endMsg();
	serialPort->write("\r\n");
}

void SerialTagValueTransport::close() {
	currentField.msgType = UNKNOWN_MSG_TYPE;
	currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
}
