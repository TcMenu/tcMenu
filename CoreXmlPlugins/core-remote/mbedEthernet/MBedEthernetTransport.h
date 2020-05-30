/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#ifndef TCMENU_MBEDETHERNETTRANSPORT_H
#define TCMENU_MBEDETHERNETTRANSPORT_H

#include <mbed.h>
#include <EthernetInterface.h>
#include <TCPServer.h>
#include <TCPSocket.h>

#include <RemoteConnector.h>
#include <TaskManager.h>

#define WRITE_DELAY 150

/**
 * An mbed implementation of tag val transport that works over a socket connection
 */
class MBedEthernetTransport : public TagValueTransport, public Executable {
private:
    char writeBuf[128];
    char readBuf[128];
    size_t readPos;
    size_t lastReadAmt;
    size_t writePos;
    InternetSocket* socket;
    bool isOpen;
public:
    MBedEthernetTransport() {
        readPos = lastReadAmt = writePos = 0;
        this->socket = NULL;
        isOpen = false;
    }

    ~MBedEthernetTransport() override;

    void setSocket(InternetSocket* sock) {
        delete socket;
        socket = sock;
        socket->set_blocking(false);
        isOpen = true;
    }

    void flush() override;

    int writeChar(char data) override;

    int writeStr(const char *data) override;

    uint8_t readByte() override;

    bool readAvailable() override;

    bool available() override;

    bool connected() override;

    void close() override;

    void endMsg() override;

    void exec() override {
        if(isOpen && writePos > 0) flush();
    }
};

class EthernetTagValServer : public Executable {
private:
    TagValueRemoteConnector connector;
    MBedEthernetTransport transport;
    CombinedMessageProcessor messageProcessor;
    TCPSocket server;
    NetworkInterface* defNetwork;
    bool boundToAddr;
    int listenPort;
public:
    enum ConnectivityState { NO_INTERFACE, INTERFACE_UP, NOT_BOUND};

    /**
     * Empty constructor - see begin.
     */
    EthernetTagValServer() : messageProcessor(msgHandlers, MSG_HANDLERS_SIZE)  {
        defNetwork = NetworkInterface::get_default_instance();
        boundToAddr = false;
    }

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
    void begin(int listenPort, const ConnectorLocalInfo* localInfo);

    /**
     * @return the EthernetTagValTransport for the given connection number - zero based
     */
    MBedEthernetTransport* getTransport(int /*num*/) { return &transport; }

    /**
     * @return the selected connector by remoteNo - zero based
     */
    TagValueRemoteConnector* getRemoteConnector(int /*num*/) { return &connector; }

    /**
     * do not manually call, called by taskManager to poll the connection
     */
    void exec() override;
};

/**
 * This is the global instance of the remote server for ethernet.
 */
extern EthernetTagValServer remoteServer;

#endif //TCMENU_MBEDETHERNETTRANSPORT_H
