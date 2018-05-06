/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * RemoteConnector.h - contains the base functionality for communication between the menu library
 * and remote APIs.
 */
#ifndef _TCMENU_REMOTECONNECTOR_H_
#define _TCMENU_REMOTECONNECTOR_H_

#include "RemoteTypes.h"
#include "MenuItems.h"

#define TICK_INTERVAL 25
#define HEARTBEAT_INTERVAL_TICKS (10000 / TICK_INTERVAL)

class ConnectorListener {
public:
	virtual ~ConnectorListener() {;}
	virtual void newJoiner(const char* name, const char* version) = 0;
	virtual void heartbeat() = 0;
	virtual void error(uint8_t type) = 0;
};

class RemoteConnector {
public:
	virtual ~RemoteConnector() {;}
	virtual void start() = 0;
	virtual void encodeJoin(const char* name, const char* version) = 0;
	virtual void encodeBootstrap(bool isComplete) = 0;
	virtual void encodeHeartbeat() = 0;
	virtual void encodeAnalogItem(int parentId, AnalogMenuItem* item) = 0;
	virtual bool isTransportAvailable() = 0;
	virtual bool isTransportConnected() = 0;
};

enum FieldValueType : byte {
	FVAL_NEW_MSG, FVAL_END_MSG, FVAL_FIELD, FVAL_ERROR_PROTO,
	// below are internal only states, and should not be acted upon.
	FVAL_PROCESSING, FVAL_PROCESSING_WAITEQ, FVAL_PROCESSING_VALUE
};

struct FieldAndValue {
	FieldValueType fieldType;
	union {
		uint16_t msgType;
		char msgTyArr[2];
	};
	uint16_t field;
	char value[MAX_VALUE_LEN];
	uint8_t len;
};

class TagValueTransport {
protected:
	FieldAndValue currentField;
public:
	virtual ~TagValueTransport() {}

	virtual void startMsg(uint16_t msgType) = 0;
	virtual void writeField(uint16_t field, const char* value) = 0;
	virtual void writeFieldP(uint16_t field, const char* value) = 0;
	virtual void writeFieldInt(uint16_t field, int value) = 0;
	virtual void endMsg() = 0;

	virtual FieldAndValue* fieldIfAvailable() = 0;

	virtual bool available() = 0;
	virtual bool connected() = 0;
};

class SerialTagValueTransport : public TagValueTransport {
private:
	HardwareSerial* serialPort;
public:
	SerialTagValueTransport(HardwareSerial* serialPort);
	virtual ~SerialTagValueTransport() {}

	virtual void startMsg(uint16_t msgType);
	virtual void writeField(uint16_t field, const char* value);
	virtual void writeFieldP(uint16_t field, const char* value);
	virtual void writeFieldInt(uint16_t field, int value);
	virtual void endMsg();

	virtual FieldAndValue* fieldIfAvailable();

	virtual bool available() { return serialPort->availableForWrite();}
	virtual bool connected() { return true;}
};

class TagValueRemoteConnector : public RemoteConnector {
private:
	char remoteName[10];
	char remoteVer[10];
	MenuItem* bootMenuPtr;
	uint16_t ticksLastSend;
	uint16_t ticksLastRead;
	TagValueTransport* transport;
	ConnectorListener* listener;
	static TagValueRemoteConnector* _TAG_INSTANCE;
public:
	TagValueRemoteConnector(TagValueTransport* transport);
	virtual void start();
	virtual ~TagValueRemoteConnector() {;}

	void setListener(ConnectorListener* listener) { this->listener = listener; }

	virtual bool isTransportAvailable() { return transport->available(); }
	virtual bool isTransportConnected() { return transport->connected(); }

	virtual void encodeJoin(const char* name, const char* version);
	virtual void encodeBootstrap(bool isComplete);
	virtual void encodeHeartbeat();
	virtual void encodeAnalogItem(int parentId, AnalogMenuItem* item);

	void tick();

private:
	void fieldForMsg(FieldAndValue* field);
	void completeMsgRx(FieldAndValue* field);
};

#endif /* _TCMENU_REMOTECONNECTOR_H_ */
