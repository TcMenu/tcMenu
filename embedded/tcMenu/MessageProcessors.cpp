/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * MessageProcessors.cpp - standard message processors that decode tcMenu messages.
 */
#include "RemoteConnector.h"
#include "MessageProcessors.h"

ValueChangeMessageProcessor valueProcessor(NULL);
HeartbeatProcessor heartbeatProcessor(&valueProcessor);
JoinMessageProcessor rootProcessor(&heartbeatProcessor);

void JoinMessageProcessor::initialise() {
	this->major = this->minor = -1;
	this->platform = PLATFORM_ARDUINO_8BIT;
}

void JoinMessageProcessor::fieldRx(FieldAndValue* field) {
	TagValueRemoteConnector::instance()->initiateBootstrap(menuMgr.getRoot());
	switch(field->field) {
	case FIELD_MSG_NAME:
		if(TagValueRemoteConnector::instance()->getListener()) TagValueRemoteConnector::instance()->getListener()->remoteNameChange(field->value);
		break;
	case FIELD_VERSION: {
		int val = atoi(field->value);
		major = val / 100;
		minor = val % 100;
		break;
	}
	case FIELD_PLATFORM:
		platform = (ApiPlatform) atoi(field->value);
		break;
	}
}

void JoinMessageProcessor::onComplete() {
	if(TagValueRemoteConnector::instance()->getListener()) TagValueRemoteConnector::instance()->getListener()->newJoiner(major, minor, platform);
}

void ValueChangeMessageProcessor::initialise() {
	this->parentId = 0;
	this->item = NULL;
	this->changeType = CHANGE_ABSOLUTE;
	this->changeValue = 0;
}

MenuItem* findItem(MenuItem* itm, int id) {
	while(itm != NULL && itm->getId() != id) {
		itm = itm->getNext();
	}
	return itm;
}

void ValueChangeMessageProcessor::fieldRx(FieldAndValue* field) {
	switch(field->field) {
	case FIELD_PARENT:
		parentId = atoi(field->value);
		break;
	case FIELD_ID: {
		int id = atoi(field->value);
		MenuItem* sub;
		if(parentId != 0) {
			sub = findItem(menuMgr.getRoot(), parentId);
			if(sub == NULL || sub->getMenuType() != MENUTYPE_SUB_VALUE) return;
			sub = ((SubMenuItem*)sub)->getChild();
		}
		else {
			sub = menuMgr.getRoot();
		}
		item = findItem(sub, id);
		break;
	}
	case FIELD_CURRENT_VAL:
		if((item != NULL) && (item->getMenuType() == MENUTYPE_INT_VALUE || item->getMenuType() == MENUTYPE_ENUM_VALUE)) {
			auto valItem = (ValueMenuItem<AnalogMenuInfo>*)item;
			auto newValue = atoi(field->value);
			if(changeType == CHANGE_ABSOLUTE) {
				valItem->setCurrentValue(newValue); // for absolutes, assume other system did checking.
			}
			else {
				// here we must do the checking, as it is a delta
				if((newValue < 0) && abs(newValue) > valItem->getCurrentValue()) return;
				newValue = valItem->getCurrentValue() + newValue;
				if(newValue > valItem->getMaximumValue()) return;
				valItem->setCurrentValue(newValue);
			}
		}
		else if(item != NULL && item->getMenuType() == MENUTYPE_BOOLEAN_VALUE) {
			// booleans are always absolute
			((BooleanMenuItem*)item)->setBoolean(atoi(field->value));
		}
		break;
	case FIELD_CHANGE_TYPE:
		changeType = (ChangeType) atoi(field->value);
		break;
	}
}

void ValueChangeMessageProcessor::onComplete() {
	menuMgr.menuItemChanged(item);
}

void HeartbeatProcessor::onComplete() {
	if(TagValueRemoteConnector::instance()->getListener()) TagValueRemoteConnector::instance()->getListener()->heartbeat();
}
