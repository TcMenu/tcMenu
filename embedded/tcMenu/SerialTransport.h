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
	SerialTagValueTransport();
	virtual ~SerialTagValueTransport() {}
	void setStream(Stream* stream) {this->serialPort = stream; }

	virtual void flush()                   {serialPort->flush();}
	virtual int writeChar(char data)       { return serialPort->write(data); }
	virtual int writeStr(const char* data) { return serialPort->write(data); }

	virtual uint8_t readByte()   { return serialPort->read(); }
	virtual bool readAvailable() { return serialPort->available(); }
	virtual bool available()     { return serialPort->availableForWrite();}
	virtual bool connected()     { return true;}

	virtual void close();

};

class SerialTagValServer {
private:
	SerialTagValueTransport serPort;
	TagValueRemoteConnector connector;
public:
	SerialTagValServer();
	void begin(Stream* portStream, const char* namePgm);
	void runLoop();
};

extern SerialTagValServer serialServer;

#endif /* _TCMENU_SERIALTRANSPORT_H_ */
