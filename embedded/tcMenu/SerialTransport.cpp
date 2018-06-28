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

bool SerialTagValueTransport::findNextMessageStart() {
	char read = 0;
	while(serialPort->available() && read != '`') {
		read = serialPort->read();
	}
	return (read == '`');
}

bool SerialTagValueTransport::processMsgKey() {
	if(highByte(currentField.field) == UNKNOWN_FIELD_PART && serialPort->available()) {
		char r = serialPort->read();
		if(r == '~') {
			currentField.fieldType = FVAL_END_MSG;
			return false;
		}
		else {
			currentField.field = ((uint16_t)r) << 8;
		}
	}

	// if we are PROCESSING the key and we've already filled in the top half, then now we need the lower part.
	if(highByte(currentField.field) != UNKNOWN_FIELD_PART && lowByte(currentField.field) == UNKNOWN_FIELD_PART && serialPort->available()) {
		currentField.field |= ((serialPort->read()) & 0xff);
		currentField.fieldType = FVAL_PROCESSING_WAITEQ;
	}

	return true;
}

bool SerialTagValueTransport::processValuePart() {
	char current = 0;
	while(serialPort->available() && current != '|') {
		current = serialPort->read();
		if(current != '|') {
			currentField.value[currentField.len] = current;
			// safety check for too much data!
			if(++currentField.len > (sizeof(currentField.value)-1)) {
				return false;
			}
		}
	}

	// reached end of field?
	if(current == '|') {
		currentField.value[currentField.len] = 0;

		// if this is a new message and the first field is not the type, that's an error
		if(currentField.msgType == UNKNOWN_MSG_TYPE && currentField.field != FIELD_MSG_TYPE) {
			return false;
		}

		// if its the message type field, populate it and report new message, otherwise report regular field
		if(currentField.field == FIELD_MSG_TYPE) {
			currentField.msgType = msgFieldToWord(currentField.value[0], currentField.value[1]);
			currentField.fieldType = FVAL_NEW_MSG;
		}
		else currentField.fieldType = FVAL_FIELD;
	}
	return true;
}

FieldAndValue* SerialTagValueTransport::fieldIfAvailable() {
	bool contProcessing = true;
	while(contProcessing) {
		switch(currentField.fieldType) {
		case FVAL_END_MSG:
			clearFieldStatus(FVAL_PROCESSING_AWAITINGMSG);
			break;
		case FVAL_ERROR_PROTO:
		case FVAL_PROCESSING_AWAITINGMSG: // in these states we need to find the next message
			if(findNextMessageStart()) {
				clearFieldStatus();
			}
			else {
				currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
				return &currentField;
			}
			break;

		case FVAL_NEW_MSG:
		case FVAL_FIELD: // the field finished last time around, now reset it.
			currentField.fieldType = FVAL_PROCESSING;
			currentField.field = UNKNOWN_FIELD_PART;
			break;

		case FVAL_PROCESSING: // we are looking for the field key
			contProcessing = processMsgKey();
			break;

		case FVAL_PROCESSING_WAITEQ: // we expect an = following the key
			if(!serialPort->available()) break;
			if(serialPort->read() != '=') {
				clearFieldStatus(FVAL_ERROR_PROTO);
				return &currentField;
			}
			currentField.len = 0;
			currentField.fieldType = FVAL_PROCESSING_VALUE;
			break;

		case FVAL_PROCESSING_VALUE: // and lastly a value followed by pipe.
			if(!processValuePart()) {
				clearFieldStatus(FVAL_ERROR_PROTO);
			}
			if(currentField.fieldType != FVAL_PROCESSING_VALUE) return &currentField;
			break;
		}
		contProcessing = contProcessing && serialPort->available();
	}
	return &currentField;
}

void SerialTagValueTransport::endMsg() {
	TagValueTransport::endMsg();
	serialPort->write("\r\n");
}
