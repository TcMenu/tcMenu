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

extern const PROGMEM char applicationVersion[] = "ard8_V1";

TagValueRemoteConnector::TagValueRemoteConnector(MenuItem* firstItem, const char* name, TagValueTransport* transport) {
	this->localName = name;
	this->listener = NULL;
	this->transport = transport;
	this->ticksLastRead = this->ticksLastSend = 0;
	this->currentlyConnected = false;
	this->firstItem = firstItem;
	this->preSubMenuBootPtr = NULL;
	this->bootMenuPtr = NULL;
}

TagValueRemoteConnector* TagValueRemoteConnector::_TAG_INSTANCE;

void TagValueRemoteConnector::start() {
	_TAG_INSTANCE = this;
	taskManager.scheduleFixedRate(TICK_INTERVAL, [] {_TAG_INSTANCE->tick();});
}

void TagValueRemoteConnector::nextBootstrap() {
	int parentId = (preSubMenuBootPtr != NULL) ? preSubMenuBootPtr->getId() : 0;
	switch(bootMenuPtr->getMenuType()) {
	case MENUTYPE_SUB_VALUE:
		encodeSubMenu(parentId, (SubMenuItem*)bootMenuPtr);
		preSubMenuBootPtr = bootMenuPtr;
		bootMenuPtr = ((SubMenuItem*)bootMenuPtr)->getChild();
		break;
	case MENUTYPE_BOOLEAN_VALUE:
		encodeBooleanMenu(parentId, (BooleanMenuItem*)bootMenuPtr);
		break;
	case MENUTYPE_ENUM_VALUE:
		encodeEnumMenu(parentId, (EnumMenuItem*)bootMenuPtr);
		break;
	case MENUTYPE_INT_VALUE:
		encodeAnalogItem(parentId, (AnalogMenuItem*)bootMenuPtr);
		break;
	default:
		break;
	}

	// see if there's more to do, including moving between submenu / root.
	bootMenuPtr = bootMenuPtr->getNext();
	if(bootMenuPtr == NULL && preSubMenuBootPtr != NULL) {
		bootMenuPtr = preSubMenuBootPtr->getNext();
	}

	// and we are done! tell the remote party
	if(!bootMenuPtr) encodeBootstrap(true);
}

void TagValueRemoteConnector::tick() {
	++ticksLastRead;
	++ticksLastSend;

	if(ticksLastSend > HEARTBEAT_INTERVAL_TICKS) {
		if(transport->available()) encodeHeartbeat();
	}

	if(ticksLastRead > (HEARTBEAT_INTERVAL_TICKS * 3)) {
		if(currentlyConnected) {
			listener->error(REMOTE_ERR_NO_HEARTBEAT);
			currentlyConnected = false;
			if(listener) listener->connected(false);
		}
	} else if(!currentlyConnected){
		encodeJoin();
		currentlyConnected = true;
		if(listener) listener->connected(true);
	}


	if(bootMenuPtr) {
		nextBootstrap();
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
		if(listener) listener->error(REMOTE_ERR_PROTOCOL_WRONG);
		break;
	default: // not ready for processing yet.
		break;
	}
}

void TagValueRemoteConnector::fieldForMsg(FieldAndValue* field) {
	switch(field->msgType) {
	case MSG_JOIN:
		if(field->field == FIELD_MSG_NAME) {
			strncpy(remoteName, field->value, MAX_DESC_SIZE);
			remoteName[MAX_DESC_SIZE - 1] = 0;
		}
		else if(field->field == FIELD_VERSION) {
			strncpy(remoteVer, field->value, MAX_DESC_SIZE);
			remoteVer[MAX_DESC_SIZE - 1] = 0;
		}
		break;
	default:
		break;
	}
}

void TagValueRemoteConnector::completeMsgRx(FieldAndValue* field) {
	switch(field->msgType) {
	case MSG_JOIN:
		// instigate bootstrap on new joiner
		bootMenuPtr = firstItem;
		if(listener) listener->newJoiner(remoteName, remoteVer);
		break;
	case MSG_HEARTBEAT:
		if(listener) listener->heartbeat();
		break;
	}

	// every message including heartbeat sets the tick back to 0.
	ticksLastRead = 0;
}

void TagValueRemoteConnector::encodeJoin() {
	if(transport->connected()) {
		transport->startMsg(MSG_JOIN);
		transport->writeFieldP(FIELD_MSG_NAME, localName);
		transport->writeFieldP(FIELD_VERSION, applicationVersion);
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
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getCurrentValue());
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

void TagValueRemoteConnector::encodeEnumMenu(int parentId, EnumMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_ENUM);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getCurrentValue());
		uint8_t noChoices = pgm_read_byte_near(&item->getMenuInfo()->noOfItems);
		transport->writeFieldInt(FIELD_NO_CHOICES, noChoices);
		for(uint8_t i=0;i<noChoices;++i) {
			char** itemPtr = ((char**)pgm_read_ptr_near(&item->getMenuInfo()->menuItems)) + i;
			uint16_t choiceKey = msgFieldToWord(FIELD_PREPEND_CHOICE, 'A' + i);
			transport->writeFieldP(choiceKey, (const char *)pgm_read_ptr_near(itemPtr));
		}
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

void TagValueRemoteConnector::encodeBooleanMenu(int parentId, BooleanMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_BOOL);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getBoolean());
		transport->writeFieldInt(FIELD_BOOL_NAMING, pgm_read_byte_near(item->getBooleanMenuInfo()->naming));
		transport->endMsg();
		ticksLastSend = 0;
	}
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}

void TagValueRemoteConnector::encodeSubMenu(int parentId, SubMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_SUBMENU);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
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

void SerialTagValueTransport::processMsgKey() {
	if(highByte(currentField.field) == UNKNOWN_FIELD_PART && serialPort->available()) {
		char r = serialPort->read();
		if(r == '~') {
			currentField.fieldType = FVAL_END_MSG;
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
			processMsgKey();
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
		contProcessing = serialPort->available();
	}
	return &currentField;
}
