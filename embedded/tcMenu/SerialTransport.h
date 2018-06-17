/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 * SerialTransport.h - the serial wire transport for a TagValueConnector.
 */

#ifndef _TCMENU_SERIALTRANSPORT_H_
#define _TCMENU_SERIALTRANSPORT_H_

#include <Arduino.h>
#include "RemoteConnector.h"

class SerialTagValueTransport : public TagValueTransport {
private:
	Stream* serialPort;
public:
	SerialTagValueTransport(Stream* serialPort);
	virtual ~SerialTagValueTransport() {}

	virtual void startMsg(uint16_t msgType);
	virtual void writeField(uint16_t field, const char* value);
	virtual void writeFieldP(uint16_t field, const char* value);
	virtual void writeFieldInt(uint16_t field, int value);
	virtual void endMsg();

	virtual FieldAndValue* fieldIfAvailable();

	virtual bool available() { return serialPort->availableForWrite();}
	virtual bool connected() { return true;}
private:
	bool findNextMessageStart();
	void clearFieldStatus(FieldValueType ty = FVAL_PROCESSING);
	bool processMsgKey();
	bool processValuePart();
};


#endif /* _TCMENU_SERIALTRANSPORT_H_ */
