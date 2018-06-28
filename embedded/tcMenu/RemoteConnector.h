/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * RemoteConnector.h - contains the base functionality for communication between the menu library
 * and remote APIs.
 */
#ifndef _TCMENU_REMOTECONNECTOR_H_
#define _TCMENU_REMOTECONNECTOR_H_

#include <Arduino.h>
#include "RemoteTypes.h"
#include "MenuItems.h"

#define TICK_INTERVAL 50
#define HEARTBEAT_INTERVAL_TICKS (10000 / TICK_INTERVAL)

class ConnectorListener {
public:
	virtual ~ConnectorListener() {;}
	virtual void remoteNameChange(const char* remoteName) = 0;
	virtual void newJoiner(uint8_t major, uint8_t minor, ApiPlatform platform) = 0;
	virtual void heartbeat() = 0;
	virtual void error(uint8_t type) = 0;
	virtual void connected(bool) = 0;
};

class RemoteConnector {
public:
	virtual ~RemoteConnector() {;}
	virtual void setListener(ConnectorListener* listener) = 0;
	virtual ConnectorListener* getListener() = 0;
	virtual void start() = 0;

	virtual void encodeJoinP(const char* localName) = 0;
	virtual void encodeBootstrap(bool isComplete) = 0;
	virtual void encodeHeartbeat() = 0;
	virtual void encodeAnalogItem(int parentId, AnalogMenuItem* item) = 0;
	virtual void encodeSubMenu(int parentId, SubMenuItem* item) = 0;
	virtual void encodeBooleanMenu(int parentId, BooleanMenuItem* item) = 0;
	virtual void encodeEnumMenu(int parentId, EnumMenuItem* item) = 0;
	virtual void encodeChangeValue(int parentId, MenuItem* theItem) = 0;

	virtual bool isTransportAvailable() = 0;
	virtual bool isTransportConnected() = 0;
};

enum FieldValueType : byte {
	FVAL_NEW_MSG, FVAL_END_MSG, FVAL_FIELD, FVAL_ERROR_PROTO,
	// below are internal only states, and should not be acted upon.
	FVAL_PROCESSING, FVAL_PROCESSING_WAITEQ, FVAL_PROCESSING_VALUE, FVAL_PROCESSING_AWAITINGMSG
};

struct FieldAndValue {
	FieldValueType fieldType;
	uint16_t msgType;
	uint16_t field;
	char value[MAX_VALUE_LEN];
	uint8_t len;
};

class TagValueTransport {
protected:
	FieldAndValue currentField;
public:
	TagValueTransport();
	virtual ~TagValueTransport() {}

	virtual void startMsg(uint16_t msgType);
	virtual void writeField(uint16_t field, const char* value);
	virtual void writeFieldP(uint16_t field, const char* value);
	virtual void writeFieldInt(uint16_t field, int value);
	virtual void endMsg();

	virtual void flush() = 0;

	virtual int writeChar(char data) = 0;
	virtual int writeStr(const char* data) = 0;
	virtual FieldAndValue* fieldIfAvailable() = 0;

	virtual bool available() = 0;
	virtual bool connected() = 0;
	void clearFieldStatus(FieldValueType ty = FVAL_PROCESSING);
};


/**
 * Instead of each type of message processing being hardwired into the connector, the custom processing
 * required for each message in instead handled by a message processor.
 */
class MessageProcessor {
protected:
	uint16_t msgType;
	MessageProcessor* next;
public:
	virtual ~MessageProcessor() {;}
	virtual void initialise() = 0;
	virtual void fieldRx(FieldAndValue* field) = 0;
	virtual void onComplete() = 0;

	// private list functions, to allow making a linked list of these.

	MessageProcessor* getNext() { return next; }
	MessageProcessor* findProcessorForType(uint16_t msgType) {
		MessageProcessor* proc = this;
		while(proc && proc->msgType != msgType) {
			proc = proc->getNext();
		}
		return proc;
	}
};

#define FLAG_CURRENTLY_CONNECTED 1
#define FLAG_BOOTSTRAP_MODE 2
#define FLAG_WRITING_MSGS 3

class TagValueRemoteConnector : public RemoteConnector {
private:
	const char* localNamePgm;
	uint16_t ticksLastSend;
	uint16_t ticksLastRead;
	uint8_t flags;
	MessageProcessor* processor;
	TagValueTransport* transport;
	ConnectorListener* listener;
	static TagValueRemoteConnector* _TAG_INSTANCE;

	// for bootstrapping
	MenuItem* bootMenuPtr;
	MenuItem* preSubMenuBootPtr;
public:
	TagValueRemoteConnector(const char* namePgm, TagValueTransport* transport);
	virtual void start();
	virtual ~TagValueRemoteConnector() {;}
	static TagValueRemoteConnector* instance() { return _TAG_INSTANCE; }

	virtual void setListener(ConnectorListener* listener) { this->listener = listener; }
	virtual ConnectorListener* getListener() { return listener; }

	virtual bool isTransportAvailable() { return transport->available(); }
	virtual bool isTransportConnected() { return transport->connected(); }

	virtual void encodeJoinP(const char* localName);
	virtual void encodeBootstrap(bool isComplete);
	virtual void encodeHeartbeat();
	virtual void encodeAnalogItem(int parentId, AnalogMenuItem* item);
	virtual void encodeSubMenu(int parentId, SubMenuItem* item);
	virtual void encodeBooleanMenu(int parentId, BooleanMenuItem* item);
	virtual void encodeTextMenu(int parentId, TextMenuItem* item);
	virtual void encodeEnumMenu(int parentId, EnumMenuItem* item);
	virtual void encodeChangeValue(int parentId, MenuItem* theItem);

	void tick();

	void initiateBootstrap(MenuItem* firstItem);

private:
	void nextBootstrap();
	void performAnyWrites();
	void dealWithHeartbeating();
	void setBootstrapMode(bool mode) { bitWrite(flags, FLAG_BOOTSTRAP_MODE, mode); }
	void setConnected(bool mode) { bitWrite(flags, FLAG_CURRENTLY_CONNECTED, mode); }
	bool isConnected() { return bitRead(flags, FLAG_CURRENTLY_CONNECTED); }
	bool isBootstrapMode() { return bitRead(flags, FLAG_BOOTSTRAP_MODE); }
};

#endif /* _TCMENU_REMOTECONNECTOR_H_ */
