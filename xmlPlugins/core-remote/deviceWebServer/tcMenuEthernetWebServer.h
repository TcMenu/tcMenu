
#ifndef TCLIBRARYDEV_TCMENUECWEBSERVER_H
#define TCLIBRARYDEV_TCMENUECWEBSERVER_H

#include <Arduino.h>
#include <STM32Ethernet.h>
#include <remote/TcMenuWebServer.h>
#include <remote/BaseRemoteComponents.h>

#define AVAILABLE_FOR_WRITE_WORKS false

/**
 * This global method absolutely must be provided when you are not using the pre-packaged version of embedCONTROL-JS
 * @param server The server reference to provide the static hosting and configuration details to.
 */

namespace tcremote {

    class TcMenuWebSockTransport : public tcremote::AbstractWebSocketTcMenuTransport {
    private:
        EthernetClient client;
        bool isConsideredOpen = false;
    public:
        TcMenuWebSockTransport() = default;

        void setClient(EthernetClient newClient) {
            client = newClient;
            isConsideredOpen = true;
            setState(tcremote::WSS_HTTP_REQUEST);
        }

        bool available() override {
#if AVAILABLE_FOR_WRITE_WORKS == true
            return client.availableForWrite();
#else
            return connected();
#endif
        }

        void close() override {
            AbstractWebSocketTcMenuTransport::close();
            client.stop();
            isConsideredOpen = false;
        }

        bool connected() override {
            return isConsideredOpen && client.connected();
        }

        int performRawRead(uint8_t *buffer, size_t bufferSize) override {
            return client.read(buffer, bufferSize);
        }

        int performRawWrite(const uint8_t *data, size_t dataSize) override {
            return (int) client.write(data, dataSize);
        }
    };

    class TcMenuWebSockInitialisation : public tcremote::AbstractWebSocketTcMenuInitialisation {
    private:
        EthernetServer *server;
    public:
        TcMenuWebSockInitialisation(EthernetServer *server, const char *expectedPath)
                : AbstractWebSocketTcMenuInitialisation(expectedPath), server(server) {}

        bool attemptInitialisation() override;

        bool attemptNewConnection(tcremote::BaseRemoteServerConnection *remoteConnection) override;
    };

    class TcMenuWebServer : public tcremote::AbstractLightweightWebServer {
    private:
        TcMenuWebSockTransport transport;
        EthernetServer *server;
        unsigned long timeStart;
    public:
        explicit TcMenuWebServer(EthernetServer *server);

        tcremote::AbstractWebSocketTcMenuTransport *attemptNewConnection() override;

        void initialiseConnection() override;
    };

    int fromWiFiRSSITo4StateIndicator(int strength);
}

void prepareWebServer(tcremote::TcMenuWebServer& webServer);

#endif //TCLIBRARYDEV_TCMENUECWEBSERVER_H
