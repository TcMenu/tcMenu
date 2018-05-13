/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * MessageProcessors.h - standard message processors that decode tcMenu messages.
 */

#ifndef _TCMENU_MESSAGEPROCESSORS_H_
#define _TCMENU_MESSAGEPROCESSORS_H_

#include <Arduino.h>
#include "tcMenu.h"
#include "RemoteConnector.h"

class ValueChangeMessageProcessor : public MessageProcessor {
private:
	MenuItem* item;
	int parentId;
	ChangeType changeType;
	int changeValue;
public:
	virtual ~ValueChangeMessageProcessor() {;}
	virtual void initialise();
	virtual void fieldRx(FieldAndValue* field);
	virtual void onComplete();

};

class JoinMessageProcessor : public MessageProcessor {
private:
	uint8_t major, minor;
	ApiPlatform platform;
public:
	virtual ~JoinMessageProcessor() {;}
	virtual void initialise();
	virtual void fieldRx(FieldAndValue* field);
	virtual void onComplete();
};

class HeartbeatProcessor : public MessageProcessor {
public:
	virtual ~HeartbeatProcessor() {;}
	virtual void initialise() {;}
	virtual void fieldRx() {;}
	virtual void onComplete();
};

extern MessageProcessor* processorList;

#endif /* _TCMENU_MESSAGEPROCESSORS_H_ */
