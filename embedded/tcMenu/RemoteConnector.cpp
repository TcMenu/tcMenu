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

#define majorminor(maj, min) ((maj * 100) + min)

int apiVersion = majorminor(0, 4);

TagValueRemoteConnector::TagValueRemoteConnector(const char* namePgm, TagValueTransport* transport) {
	this->localNamePgm = namePgm;
	this->listener = NULL;
	this->transport = transport;
	this->ticksLastRead = this->ticksLastSend = 0xffff;
	this->flags = 0;
	this->processor = NULL;
	this->bootMenuPtr = preSubMenuBootPtr = NULL;
}

TagValueRemoteConnector* TagValueRemoteConnector::_TAG_INSTANCE;

void TagValueRemoteConnector::start() {
	_TAG_INSTANCE = this;
	taskManager.scheduleFixedRate(TICK_INTERVAL, [] {_TAG_INSTANCE->tick();});
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
		if(processor) processor->onComplete();
		processor = NULL;
		ticksLastRead = 0;
		break;
	case FVAL_ERROR_PROTO:
		if(listener) listener->error(REMOTE_ERR_PROTOCOL_WRONG);
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
		if(transport->available()) encodeHeartbeat();
	}

	if(ticksLastRead > (HEARTBEAT_INTERVAL_TICKS * 3)) {
		if(isConnected()) {
			listener->error(REMOTE_ERR_NO_HEARTBEAT);
			setConnected(false);
			processor = NULL;
			if(listener) listener->connected(false);
		}
	} else if(!isConnected()){
		encodeJoinP(localNamePgm);
		processor = NULL;
		setConnected(true);
		if(listener) listener->connected(true);
	}

}

void TagValueRemoteConnector::performAnyWrites() {
	if(isBootstrapMode()) {
		nextBootstrap();
	}
	else {
		if(bootMenuPtr == NULL) bootMenuPtr = menuMgr.getRoot();

		int parentId = (preSubMenuBootPtr != NULL) ? preSubMenuBootPtr->getId() : 0;
		if(bootMenuPtr->isSendRemoteNeeded()) {
			bootMenuPtr->setSendRemoteNeeded(false);
			encodeChangeValue(parentId, bootMenuPtr);
		}

		// see if there's more to do, including moving between submenu / root.
		bootMenuPtr = bootMenuPtr->getNext();
		if(bootMenuPtr == NULL && preSubMenuBootPtr != NULL) {
			bootMenuPtr = preSubMenuBootPtr->getNext();
			preSubMenuBootPtr = NULL;
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
	bootMenuPtr->setSendRemoteNeeded(false);
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
		transport->writeFieldInt(FIELD_VERSION, apiVersion);
		transport->writeFieldInt(FIELD_PLATFORM, PLATFORM_ARDUINO_8BIT);
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

void TagValueRemoteConnector::encodeTextMenu(int parentId, TextMenuItem* item) {
	if(transport->connected()) {
		transport->startMsg(MSG_BOOT_TEXT);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID,item->getId());
		transport->writeFieldP(FIELD_MSG_NAME, item->getNamePgm());
		transport->writeFieldInt(FIELD_MAX_LEN, item->textLength());
		transport->writeField(FIELD_CURRENT_VAL, item->getTextValue());
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
		transport->writeFieldInt(FIELD_BOOL_NAMING, pgm_read_byte_near(&item->getBooleanMenuInfo()->naming));
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

void TagValueRemoteConnector::encodeChangeValue(int parentId, MenuItem* theItem) {
	if(transport->connected()) {
		transport->startMsg(MSG_CHANGE_INT);
		transport->writeFieldInt(FIELD_PARENT, parentId);
		transport->writeFieldInt(FIELD_ID, theItem->getId());
		transport->writeFieldInt(FIELD_CHANGE_TYPE, CHANGE_ABSOLUTE); // menu host always sends absolute!
		switch(theItem->getMenuType()) {
		case MENUTYPE_ENUM_VALUE:
		case MENUTYPE_INT_VALUE:
			transport->writeFieldInt(FIELD_CURRENT_VAL, ((ValueMenuItem<AnalogMenuInfo>*)theItem)->getCurrentValue());
			break;
		case MENUTYPE_BOOLEAN_VALUE:
			transport->writeFieldInt(FIELD_CURRENT_VAL, ((BooleanMenuItem*)theItem)->getBoolean());
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
	else if(listener) {
		listener->error(REMOTE_ERR_WRITE_NOT_CONNECTED);
	}
}
