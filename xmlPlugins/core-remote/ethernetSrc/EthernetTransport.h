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
#include <tcUtil.h>

#ifndef ETHERNET_BUFFER_SIZE
#define ETHERNET_BUFFER_SIZE 0
#endif

#if ETHERNET_BUFFER_SIZE > 0

/**
 * An implementation of TagValueTransport that is able to read and write via a buffer to sockets.
 */
class EthernetTagValTransport : public TagValueTransport {
private:
	EthernetClient client;
    uint8_t bufferData[ETHERNET_BUFFER_SIZE];
    int bufferPosition;
public:
	EthernetTagValTransport();
	virtual ~EthernetTagValTransport();
	void setClient(EthernetClient client) { this->client = client; }

    void endMsg() override {
        TagValueTransport::endMsg();
        flush();
    }
	int writeChar(char data) override ;
	int writeStr(const char* data) override;
	void flush() override;
	bool available() override;
	bool connected() override;
	uint8_t readByte() override;
	bool readAvailable() override;
	void close() override;
};

#else // ethernet buffering not needed

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

#endif // ethernet buffering check

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
     * Sets the mode of authentication used with your remote, if you don't call this the system will
     * default to no authentication; which is probably fine for serial / bluetooth serial.
    *
     * This should always be called before begin(), to ensure this in your code always ensure this
     * is called BEFORE setupMenu().
     *
     * @param authManager a reference to an authentication manager.
     */
    void setAuthenticator(AuthenticationManager* authManager) { connector.setAuthManager(authManager); }

	/**
	 * Creates the ethernet client manager components.
	 * @param server a ready configured ethernet server instance.
	 * @param namePgm the local name in program memory on AVR
	 */
	void begin(EthernetServer* server, const ConnectorLocalInfo* localInfo);

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

/**
 * This function converts from a RSSI (Radio Strength indicator)
 * measurement into a series of icons (of the ones we have defined
 * in the stock icons. The input is the RSSI figure in dB as an
 * integer.
 * @param strength the signal strength (usually negative) as an int
 * @return a state that can be used with the standard wifi TitleWidget
 */
int fromWiFiRSSITo4StateIndicator(int strength);

#endif /* _TCMENU_ETHERNETTRANSPORT_H_ */
