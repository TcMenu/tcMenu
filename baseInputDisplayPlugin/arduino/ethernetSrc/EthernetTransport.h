/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file EthernetTransport.h
 * 
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#ifndef _TCMENU_ETHERNETTRANSPORT_H_
#define _TCMENU_ETHERNETTRANSPORT_H_

#include <RemoteConnector.h>
#include <TaskManager.h>
#include <Ethernet.h>

/**
 * An implementation of TagValueTransport that is able to read and write using sockets.
 */
class EthernetTagValTransport : public TagValueTransport {
private:
	EthernetClient client;
public:
	EthernetTagValTransport();
	virtual ~EthernetTagValTransport();
	void setClient(EthernetClient client) { this->client = client; }

	virtual int writeChar(char data);
	virtual int writeStr(const char* data);
	virtual void flush();
	virtual bool available();
	virtual bool connected();
	virtual uint8_t readByte();
	virtual bool readAvailable();
	virtual void close();
};

/**
 * This is the actual server component that manages all the ethernet connections.
 * It holds the connector, transport and processors that are needed to be able
 * to service messages. To initialise it one calls begin(..) passing an ethernet
 * server to listen on and the name (which on AVR is in PROGMEM).
 */
class EthernetTagValServer : public Executable {
private:
	TagValueRemoteConnector connector;
	EthernetTagValTransport transport;
	CombinedMessageProcessor messageProcessor;
	EthernetServer *server;
public:

	/**
	 * Empty constructor - see begin.
	 */
	EthernetTagValServer();

	/**
	 * Creates the ethernet client manager components.
	 * @param server a ready configured ethernet server instance.
	 * @param namePgm the local name in program memory on AVR
	 */
	void begin(EthernetServer* server, const char* namePgm);

	/**
	 * @return the EthernetTagValTransport for the given connection number - zero based
	 */
	EthernetTagValTransport* getTransport(int /*num*/) { return &transport; }

	/**
	 * @return the selected connector by remoteNo - zero based
	 */
	TagValueRemoteConnector* getRemoteConnector(int /*num*/) { return &connector; }

	/**
	 * do not manually call, called by taskManager to poll the connection
	 */
	void exec();
};

/**
 * This is the global instance of the remote server for ethernet.
 */
extern EthernetTagValServer remoteServer;

#endif /* _TCMENU_ETHERNETTRANSPORT_H_ */
