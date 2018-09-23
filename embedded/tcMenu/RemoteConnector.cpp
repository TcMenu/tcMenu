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
#include "tcMenu.h"
#include "MessageProcessors.h"

const PROGMEM char EMPTYNAME[] = "Device";

CommsCallbackFn TagValueTransport::notificationFn = NULL;

TagValueRemoteConnector::TagValueRemoteConnector(TagValueTransport* transport, uint8_t remoteNo) {
	this->transport = transport;
	this->localNamePgm = EMPTYNAME;
	this->remoteNo = remoteNo;
	this->ticksLastRead = this->ticksLastSend = 0xffff;
	this->flags = 0;
	this->processor = NULL;
	this->bootMenuPtr = preSubMenuBootPtr = NULL;
}

void TagValueRemoteConnector::tick() {
	dealWithHeartbeating();

	if(isConnected()) {
		performAnyWrites();
	}

	FieldAndValue* field = transport->fieldIfAvailable();
	switch(field->fieldType) {
	case FVAL_NEW_MSG:
		processor = rootProcessor.findProcessorForType(field->msgType);
		if(processor) processor->initialise();
		break;
	case FVAL_FIELD:
		if(processor) processor->fieldRx(field);
		break;
	case FVAL_END_MSG:
		if(processor) {
			processor->onComplete();
			if(processor->requiresBootstrap()) initiateBootstrap(menuMgr.getRoot());
		}
		processor = NULL;
		ticksLastRead = 0;
		break;
	case FVAL_ERROR_PROTO:
		TagValueTransport::commsNotify(COMMS_ERR_WRONG_PROTOCOL);
		processor = NULL;
		break;
	default: // not ready for processing yet.
		break;
	}
}

void TagValueRemoteConnector::dealWithHeartbeating() {
	++ticksLastRead;
	++ticksLastSend;

	if(ticksLastSend > HEARTBEAT_INTERVAL_TICKS) {
		serdebug("sending hb");
		if(transport->available()) encodeHeartbeat();
	}

	if(ticksLastRead > (HEARTBEAT_INTERVAL_TICKS * 3)) {
		if(isConnected()) {
			setConnected(false);
			processor = NULL;
			TagValueTransport::commsNotify(COMMS_DISCONNECTED1);
			transport->close();
		}
	} else if(!isConnected()){
		encodeJoinP(localNamePgm);
		processor = NULL;
		setConnected(true);
		TagValueTransport::commsNotify(COMMS_CONNECTED1);
	}

}

void TagValueRemoteConnector::performAnyWrites() {
	if(isBootstrapMode()) {
		nextBootstrap();
	}
	else {
		if(bootMenuPtr == NULL) bootMenuPtr = menuMgr.getRoot();

		// we loop here until either we've gone through the structure or something has changed
		while(bootMenuPtr) {
			int parentId = (preSubMenuBootPtr != NULL) ? preSubMenuBootPtr->getId() : 0;
			if(bootMenuPtr->getMenuType() == MENUTYPE_SUB_VALUE) {
				preSubMenuBootPtr = bootMenuPtr;
				SubMenuItem* sub = (SubMenuItem*) bootMenuPtr;
				bootMenuPtr = sub->getChild();
			}
			else if(bootMenuPtr->isSendRemoteNeeded(remoteNo)) {
				bootMenuPtr->setSendRemoteNeeded(remoteNo, false);
				encodeChangeValue(parentId, bootMenuPtr);
				return; // exit once something is written
			}

			// see if there's more to do, including moving between submenu / root.
			bootMenuPtr = bootMenuPtr->getNext();
			if(bootMenuPtr == NULL && preSubMenuBootPtr != NULL) {
				bootMenuPtr = preSubMenuBootPtr->getNext();
				preSubMenuBootPtr = NULL;
			}
		}
	}
}

void TagValueRemoteConnector::initiateBootstrap(MenuItem* firstItem) {
	if(isBootstrapMode()) return; // already booting.

	bootMenuPtr = firstItem;
	preSubMenuBootPtr = NULL;
	encodeBootstrap(false);
	setBootstrapMode(true);
}

void TagValueRemoteConnector::nextBootstrap() {
	if(!bootMenuPtr) {
		setBootstrapMode(false);
		encodeBootstrap(true);
		preSubMenuBootPtr = NULL;
		return;
	}

	if(!transport->available()) return; // skip a turn, no write available.

	int parentId = (preSubMenuBootPtr != NULL) ? preSubMenuBootPtr->getId() : 0;
	bootMenuPtr->setSendRemoteNeeded(remoteNo, false);
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
	case MENUTYPE_TEXT_VALUE:
		encodeTextMenu(parentId, (TextMenuItem*)bootMenuPtr);
		break;
	default:
		break;
	}

	// see if there's more to do, including moving between submenu / root.
	bootMenuPtr = bootMenuPtr->getNext();
	if(bootMenuPtr == NULL && preSubMenuBootPtr != NULL) {
		bootMenuPtr = preSubMenuBootPtr->getNext();
		preSubMenuBootPtr = NULL;
	}
}

void TagValueRemoteConnector::encodeJoinP(const char* localName) {
	if(transport->connected()) {
		transport->startMsg(MSG_JOIN);
		transport->writeFieldP(FIELD_MSG_NAME, localName);
		transport->writeFieldInt(FIELD_VERSION, API_VERSION);
		transport->writeFieldInt(FIELD_PLATFORM, PLATFORM_ARDUINO_8BIT);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
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
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeHeartbeat() {
	if(transport->connected()) {
		transport->startMsg(MSG_HEARTBEAT);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeBaseMenuFields(int parentId, MenuItem* item) {
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldInt(FIELD_READONLY, item->isReadOnly());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
}

void TagValueRemoteConnector::encodeAnalogItem(int parentId, AnalogMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_ANALOG);
		encodeBaseMenuFields(parentId, item);
		transport->writeFieldP(FIELD_ANALOG_UNIT, item->getUnitNamePgm());
		transport->writeFieldInt(FIELD_ANALOG_MAX, item->getMaximumValue());
		transport->writeFieldInt(FIELD_ANALOG_OFF, item->getOffset());
		transport->writeFieldInt(FIELD_ANALOG_DIV, item->getDivisor());
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getCurrentValue());
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeTextMenu(int parentId, TextMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_TEXT);
		encodeBaseMenuFields(parentId, item);
		transport->writeFieldInt(FIELD_MAX_LEN, item->textLength());
		transport->writeField(FIELD_CURRENT_VAL, item->getTextValue());
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}


void TagValueRemoteConnector::encodeEnumMenu(int parentId, EnumMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_ENUM);
		encodeBaseMenuFields(parentId, item);
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getCurrentValue());
		uint8_t noChoices = item->getMaximumValue();
		transport->writeFieldInt(FIELD_NO_CHOICES, noChoices);
		for(uint8_t i=0;i<noChoices;++i) {
			uint16_t choiceKey = msgFieldToWord(FIELD_PREPEND_CHOICE, 'A' + i);
			transport->writeFieldP(choiceKey, (const char *)item->getEntryPgm(i));
		}
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeBooleanMenu(int parentId, BooleanMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_BOOL);
		encodeBaseMenuFields(parentId, item);
		transport->writeFieldInt(FIELD_CURRENT_VAL, item->getCurrentValue());
		transport->writeFieldInt(FIELD_BOOL_NAMING, item->getBooleanNaming());
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeSubMenu(int parentId, SubMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_SUBMENU);
		encodeBaseMenuFields(parentId, item);
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

void TagValueRemoteConnector::encodeChangeValue(int parentId, MenuItem* theItem) {
	if(transport->connected()) {
		transport->startMsg(MSG_CHANGE_INT);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID, theItem->getId());
		transport->writeFieldInt(FIELD_CHANGE_TYPE, CHANGE_ABSOLUTE); // menu host always sends absolute!
		switch(theItem->getMenuType()) {
		case MENUTYPE_ENUM_VALUE:
		case MENUTYPE_INT_VALUE:
		case MENUTYPE_BOOLEAN_VALUE:
			transport->writeFieldInt(FIELD_CURRENT_VAL, ((ValueMenuItem*)theItem)->getCurrentValue());
			break;
		case MENUTYPE_TEXT_VALUE:
			transport->writeField(FIELD_CURRENT_VAL, ((TextMenuItem*)theItem)->getTextValue());
			break;
		default:
			break;
		}
		transport->endMsg();
		ticksLastSend = 0;
	}
	else TagValueTransport::commsNotify(COMMS_ERR_WRITE_NOT_CONNECTED);
}

//
// Base transport capabilities
//

TagValueTransport::TagValueTransport() {
	this->currentField.field = UNKNOWN_FIELD_PART;
	this->currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
	this->currentField.msgType = UNKNOWN_MSG_TYPE;
	this->currentField.len = 0;
}

void TagValueTransport::startMsg(uint16_t msgType) {
	// start of message
	writeChar(START_OF_MESSAGE);

	// protocol low and high byte
	writeChar(TAG_VAL_PROTOCOL);

	char sz[3];
	sz[0] = msgType >> 8;
	sz[1] = msgType & 0xff;
	sz[2] = 0;
	writeField(FIELD_MSG_TYPE, sz);
}

void TagValueTransport::writeField(uint16_t field, const char* value) {
	char sz[4];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	writeStr(sz);
	writeStr(value);
	writeChar('|');
}

void TagValueTransport::writeFieldP(uint16_t field, const char* value) {
	char sz[4];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	writeStr(sz);

	while(char val = pgm_read_byte_near(value)) {
		writeChar(val);
		++value;
	}

	writeChar('|');
}

void TagValueTransport::writeFieldInt(uint16_t field, int value) {
	char sz[10];
	sz[0] = field >> 8;
	sz[1] = field & 0xff;
	sz[2] = '=';
	sz[3] = 0;
	writeStr(sz);
	itoa(value, sz, 10);
	writeStr(sz);
	writeChar('|');
}

void TagValueTransport::endMsg() {
	writeStr("~\n");
}

void TagValueTransport::clearFieldStatus(FieldValueType ty) {
	currentField.fieldType = ty;
	currentField.field = UNKNOWN_FIELD_PART;
	currentField.msgType = UNKNOWN_MSG_TYPE;
}

bool TagValueTransport::findNextMessageStart() {
	char read = 0;
	while(readAvailable() && read != START_OF_MESSAGE) {
		read = readByte();
	}
	return (read == START_OF_MESSAGE);
}

bool TagValueTransport::processMsgKey() {
	if(highByte(currentField.field) == UNKNOWN_FIELD_PART && readAvailable()) {
		char r = readByte();
		if(r == '~') {
			currentField.fieldType = FVAL_END_MSG;
			return false;
		}
		else {
			currentField.field = ((uint16_t)r) << 8;
		}
	}

	// if we are PROCESSING the key and we've already filled in the top half, then now we need the lower part.
	if(highByte(currentField.field) != UNKNOWN_FIELD_PART && lowByte(currentField.field) == UNKNOWN_FIELD_PART && readAvailable()) {
		currentField.field |= ((readByte()) & 0xff);
		currentField.fieldType = FVAL_PROCESSING_WAITEQ;
	}

	return true;
}

bool TagValueTransport::processValuePart() {
	char current = 0;
	while(readAvailable() && current != '|') {
		current = readByte();
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

FieldAndValue* TagValueTransport::fieldIfAvailable() {
	bool contProcessing = true;
	while(contProcessing) {
		switch(currentField.fieldType) {
		case FVAL_END_MSG:
			clearFieldStatus(FVAL_PROCESSING_AWAITINGMSG);
			break;
		case FVAL_ERROR_PROTO:
		case FVAL_PROCESSING_AWAITINGMSG: // in these states we need to find the next message
			if(findNextMessageStart()) {
				clearFieldStatus(FVAL_PROCESSING_PROTOCOL);
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

		case FVAL_PROCESSING_PROTOCOL: // we need to make sure the protocol is valid
			if(!readAvailable()) break;
			currentField.fieldType = (readByte() == TAG_VAL_PROTOCOL) ? FVAL_PROCESSING : FVAL_ERROR_PROTO;
			break;

		case FVAL_PROCESSING: // we are looking for the field key
			contProcessing = processMsgKey();
			break;

		case FVAL_PROCESSING_WAITEQ: // we expect an = following the key
			if(!readAvailable()) break;
			if(readByte() != '=') {
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
		contProcessing = contProcessing && readAvailable();
	}
	return &currentField;
}
