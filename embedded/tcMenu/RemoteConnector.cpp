/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * RemoteConnector.cpp - contains the base functionality for communication between the menu library
 * and remote APIs.
 */

#include <Arduino.h>
#include "RemoteConnector.h"
#include "MenuItems.h"
#include "TaskManager.h"

TagValueRemoteConnector::TagValueRemoteConnector(TagValueTransport* transport) {
	this->listener = NULL;
	this->bootMenuPtr = NULL;
	this->transport = transport;
	this->ticksLastRead = this->ticksLastSend = 0;
}

TagValueRemoteConnector* TagValueRemoteConnector::_TAG_INSTANCE;

void TagValueRemoteConnector::start() {
	_TAG_INSTANCE = this;
	taskManager.scheduleFixedRate(TICK_INTERVAL, [] {_TAG_INSTANCE->tick();});
}

void TagValueRemoteConnector::tick() {
	++ticksLastRead;
	++ticksLastSend;

	if(transport->connected()) {
		if(transport->available()) {
			if(ticksLastSend > HEARTBEAT_INTERVAL_TICKS) {
				encodeHeartbeat();
			}

			if(bootMenuPtr) {
				if(bootMenuPtr->getMenuType() == MENUTYPE_INT_VALUE) {
					encodeAnalogItem(0, (AnalogMenuItem*)bootMenuPtr);
				}
				bootMenuPtr = bootMenuPtr->getNext();
			}
		}

		FieldAndValue* field = transport->fieldIfAvailable();
		switch(field->fieldType) {
		case FVAL_NEW_MSG:
		case FVAL_FIELD:
			fieldForMsg(field);
			break;
		case FVAL_END_MSG:
			completeMsgRx(field);
			break;
		case FVAL_ERROR_PROTO:
			listener->error(REMOTE_ERR_PROTOCOL_WRONG);
			break;
		default:
			break;
		}
	}
	else {
		// TODO, report problems..
	}
}

void TagValueRemoteConnector::fieldForMsg(FieldAndValue* field) {
	if(field->msgType == MSG_JOIN) {
		if(field->field == FIELD_MSG_NAME) {
			strncpy(remoteName, field->value, sizeof(remoteName));
		}
		else if(field->field == FIELD_VERSION) {
			strncpy(remoteVer, field->value, sizeof(remoteVer));
		}
	}

}

void TagValueRemoteConnector::completeMsgRx(FieldAndValue* field) {
	switch(field->msgType) {
	case MSG_JOIN:
		// instigate bootstrap on new joiner
		bootMenuPtr = NULL;
		break;
	}

	// every message including heartbeat sets the tick back to 0.
	ticksLastRead = 0;
}

void TagValueRemoteConnector::encodeJoin(const char* name, const char* version) {
	if(transport->connected()) {
		transport->startMsg(MSG_JOIN);
		transport->writeField(FIELD_MSG_NAME, name);
		transport->writeField(FIELD_VERSION, version);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

const char PROGMEM pmemBootStartText[] = "START";
const char PROGMEM pmemBootEndText[] = "END";

void TagValueRemoteConnector::encodeBootstrap(bool isComplete) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOTSTRAP);
		transport->writeFieldP(FIELD_BOOT_TYPE, isComplete ?  pmemBootEndText : pmemBootStartText);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

void TagValueRemoteConnector::encodeHeartbeat() {
	if(transport->connected()) {
		transport->startMsg(MSG_HEARTBEAT);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}

}

void TagValueRemoteConnector::encodeAnalogItem(int parentId, AnalogMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_ANALOG);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
		transport->writeFieldP(FIELD_ANALOG_UNIT, item->getUnitNamePgm());
		transport->writeFieldInt(FIELD_ANALOG_MAX, pgm_read_byte_near(item->getMenuInfo()->maxValue));
		transport->writeFieldInt(FIELD_ANALOG_OFF, pgm_read_byte_near(item->getMenuInfo()->offset));
		transport->writeFieldInt(FIELD_ANALOG_DIV, pgm_read_byte_near(item->getMenuInfo()->divisor));
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

SerialTagValueTransport::SerialTagValueTransport(HardwareSerial* serialPort) {
	this->serialPort = serialPort;
	this->currentField.field = UNKNOWN_FIELD_PART;
	this->currentField.fieldType = FVAL_PROCESSING;
	this->currentField.msgType = UNKNOWN_MSG_TYPE;
	this->currentField.len = 0;
}

void SerialTagValueTransport::startMsg(uint16_t msgType) {
	char sz[3];
	sz[0] = msgType >> 8;
	sz[1] = msgType & 0xff;
	sz[2] = 0;
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

FieldAndValue* SerialTagValueTransport::fieldIfAvailable() {

	// if there's an error, then recover from the error by trying to skip the rest of the message.
	if(currentField.fieldType == FVAL_ERROR_PROTO) {
		char read=0;
		while(serialPort->available() && (read = serialPort->read()) != '~');
		if(read == '~') {
			currentField.fieldType = FVAL_END_MSG;
		}
		else {
			return &currentField;
		}
	}

	// if we finished the previous message or field, then reset to processing.
	if(currentField.fieldType == FVAL_END_MSG || currentField.fieldType == FVAL_FIELD) {
		currentField.fieldType = FVAL_PROCESSING;
		currentField.field = UNKNOWN_FIELD_PART;
		currentField.msgType = UNKNOWN_MSG_TYPE;
	}

	// now we see if we are on byte 1 of the header
	while(currentField.msgTyArr[0] == UNKNOWN_FIELD_PART && serialPort->available()) {
		char r = serialPort->read();
		if(r == '~') {
			currentField.fieldType = FVAL_END_MSG;
			return &currentField;
		}
		else if(isAlphaNumeric(r)) {
			currentField.msgTyArr[0] = r;
		}
	}

	if(currentField.msgTyArr[0] != UNKNOWN_FIELD_PART && currentField.msgTyArr[1] == UNKNOWN_FIELD_PART && serialPort->available()) {
		currentField.msgTyArr[1] = serialPort->read();
		currentField.fieldType = FVAL_PROCESSING_WAITEQ;
	}

	if(currentField.fieldType == FVAL_PROCESSING_WAITEQ && serialPort->available()) {
		currentField.len = 0;
		if(serialPort->read() != '=') {
			currentField.fieldType = FVAL_ERROR_PROTO;
			currentField.field = UNKNOWN_FIELD_PART;
			currentField.msgType = UNKNOWN_MSG_TYPE;
		}
		currentField.fieldType = FVAL_PROCESSING_VALUE;
	}
	else if(currentField.fieldType == FVAL_PROCESSING_VALUE) {
		char current = 0;
		while(serialPort->available() && (current = serialPort->read()) == '|') {
			currentField.value[currentField.len] = current;
			if(++currentField.len > MAX_VALUE_LEN) {
				currentField.fieldType = FVAL_ERROR_PROTO;
				currentField.field = UNKNOWN_FIELD_PART;
				currentField.msgType = UNKNOWN_MSG_TYPE;
				return &currentField; // get out of here - gone badly wrong.
			}
		}
		if(current == '|') {
			currentField.fieldType = FVAL_FIELD;
			if(currentField.msgType == UNKNOWN_MSG_TYPE && currentField.field != FIELD_MSG_TYPE) {
				currentField.fieldType = FVAL_ERROR_PROTO;
				currentField.field = UNKNOWN_FIELD_PART;
				currentField.msgType = UNKNOWN_MSG_TYPE;
			}

			if(currentField.field == FIELD_MSG_TYPE) {
				currentField.msgType = msgFieldToWord(currentField.value[0], currentField.value[1]);
				currentField.fieldType = FVAL_NEW_MSG;
			}
		}
	}
	return &currentField;
}
