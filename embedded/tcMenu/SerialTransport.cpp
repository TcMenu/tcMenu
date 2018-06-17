/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * SerialTransport.cpp - the serial wire transport for a TagValueConnector.
 */

#include "SerialTransport.h"

SerialTagValueTransport::SerialTagValueTransport(Stream* serialPort) {
	this->serialPort = serialPort;
	this->currentField.field = UNKNOWN_FIELD_PART;
	this->currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
	this->currentField.msgType = UNKNOWN_MSG_TYPE;
	this->currentField.len = 0;
}

void SerialTagValueTransport::startMsg(uint16_t msgType) {
	char sz[3];
	sz[0] = msgType >> 8;
	sz[1] = msgType & 0xff;
	sz[2] = 0;
	serialPort->write('`');
	writeField(FIELD_MSG_TYPE, sz);
}

void SerialTagValueTransport::writeField(uint16_t field, const char* value) {
	char sz[4];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	serialPort->write(sz);
	serialPort->write(value);
	serialPort->write('|');
}

void SerialTagValueTransport::writeFieldP(uint16_t field, const char* value) {
	char sz[4];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	serialPort->write(sz);

	while(char val = pgm_read_byte_near(value)) {
		serialPort->write(val);
		++value;
	}

	serialPort->write('|');
}

void SerialTagValueTransport::writeFieldInt(uint16_t field, int value) {
	char sz[10];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	serialPort->write(sz);
	itoa(value, sz, 10);
	serialPort->write(sz);
	serialPort->write('|');
}

void SerialTagValueTransport::endMsg() {
	serialPort->write("~\n");
}

bool SerialTagValueTransport::findNextMessageStart() {
	char read = 0;
	char cnt = '0';
	while(serialPort->available() && read != '`') {
		read = serialPort->read();
		cnt++;
	}
	return (read == '`');
}

void SerialTagValueTransport::clearFieldStatus(FieldValueType ty) {
	currentField.fieldType = ty;
	currentField.field = UNKNOWN_FIELD_PART;
	currentField.msgType = UNKNOWN_MSG_TYPE;

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




