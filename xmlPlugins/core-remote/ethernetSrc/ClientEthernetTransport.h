/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file EthernetTransport.h
 * 
 * Ethernet remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#ifndef TCMENU_ETHERNETTRANSPORT_H_
#define TCMENU_ETHERNETTRANSPORT_H_

#include <RemoteConnector.h>
#include <TaskManager.h>
#include <Ethernet.h>
#include <tcUtil.h>
#include <remote/BaseRemoteComponents.h>

#ifndef ETHERNET_BUFFER_SIZE
#define ETHERNET_BUFFER_SIZE 96
#endif // ETHERNET_BUFFER_SIZE

#ifndef DEFAULT_BACKOFF_PERIOD
#define DEFAULT_BACKOFF_PERIOD (4 * (1000 / TICK_INTERVAL))
#endif // DEFAULT_BACKOFF_PERIOD

#include <remote/BaseBufferedRemoteTransport.h>

namespace tcremote {

/**
 * An implementation of TagValueTransport that is able to read and write via a buffer to sockets.
 */
class ClientEthernetTagValTransport : public tcremote::BaseBufferedRemoteTransport {
private:
    EthernetClient client;
public:
    ClientEthernetTagValTransport() : BaseBufferedRemoteTransport(BUFFER_ONE_MESSAGE, ETHERNET_BUFFER_SIZE, MAX_VALUE_LEN) { }
    ~ClientEthernetTagValTransport() override = default;
    void setClient(EthernetClient cl) { this->client = cl; }

    int fillReadBuffer(uint8_t* data, int maxSize) override;
    void flush() override;
    bool available() override;
    bool connected() override;
    void close() override;
};


/**
 * This class provides the initialisation and connection generation logic for ethernet connections.
 */
class ClientEthernetInitialisation : public DeviceInitialisation {
private:
    EthernetClient client;
    const char* host;
    uint16_t port;
    uint16_t backOffPeriod = 0;
public:
    explicit ClientEthernetInitialisation(const char* host, uint16_t port) : host(host), port(port) {}

    bool attemptInitialisation() override;

    bool attemptNewConnection(BaseRemoteServerConnection *transport) override;
private:
    bool checkBackoffPeriod();
};

/**
 * This function converts from a RSSI (Radio Strength indicator)
 * measurement into a series of icons (of the ones we have defined
 * in the stock icons. The input is the RSSI figure in dB as an
 * integer.
 * @param strength the signal strength (usually negative) as an int
 * @return a state that can be used with the standard wifi TitleWidget
 */
int fromWiFiRSSITo4StateIndicator(int strength);

} // namespace tcremote

#ifndef TC_MANUAL_NAMESPACING
using namespace tcremote;
#endif // TC_MANUAL_NAMESPACING

#endif /* TCMENU_ETHERNETTRANSPORT_H_ */
