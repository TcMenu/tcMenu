/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * @file tcMenuEspAsyncWebServer.h
 * ESP Async Webserver remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#ifndef TCMENU_ESPASYNCWEBSERVER_H
#define TCMENU_ESPASYNCWEBSERVER_H

#include <RemoteConnector.h>
#include <remote/BaseRemoteComponents.h>
#include <remote/BaseBufferedRemoteTransport.h>
#include <ESPAsyncWebServer.h>
#include <SCCircularBuffer.h>

/**
 * This global method absolutely must be provided when you are not using the pre-packaged version of embedCONTROL-JS
 * @param server The server reference to provide the static hosting and configuration details to.
 */
void prepareWebServer(AsyncWebServer& server);

namespace tcremote {

    /**
     * This class implements the TagValTransport for a web socket connection. It is fully asynchronous and implements
     * thread safety by copying read data back to local storage through task manager.
     */
    class EspAsyncWebSocketClient : public BaseBufferedRemoteTransport, public Executable {
    private:
        AsyncWebSocket *webSocket;
        tccollection::SCCircularBuffer readThreadBuffer = tccollection::SCCircularBuffer(1024);
        volatile uint32_t wsClientId;
        volatile bool inUse;
        uint8_t remoteNum;
    public:
        explicit EspAsyncWebSocketClient(AsyncWebSocket *webSock);

        [[nodiscard]] uint32_t getClientId() const { return wsClientId; }

        int fillReadBuffer(uint8_t *dataBuffer, int maxSize) override { return 0; }

        void flush() override;

        bool available() override;

        bool connected() override { return inUse; }

        void close() override;

        /**
         * This is used to copy over the connection data from the read thread to the taskManager safe environment.
         * It means that the connection is scheduled by task manager at which time the data is copied.
         */
        void exec() override;

        /**
         * Called on the web server's own thread, we should copy the data over minimally, and then the callee should
         * trigger the event by notifying. This is locked using a simple lock to ensure that the data is not
         * replaced while being accessed.
         * @param data the data to be copied
         * @param len the length of the data to be copied.
         * @return true if the data was copied, otherwise false.
         */
        bool dataAvailable(uint8_t *data, size_t len);

        void registerConnection(uint32_t id);

        void markNotInUse();
    };

    /**
     * This uses the async ESP webserver to both serve up the static web application, and also to implement websockets so
     * that the live state can be represented.
     */
    class EspAsyncWebserver : public tcremote::DeviceInitialisation, Executable {
    private:
        AsyncWebServer webServer;
        AsyncWebSocket webSocket;
    public:
        EspAsyncWebserver(const char *websocketAddress, int port);

        bool attemptInitialisation() override;

        void exec() override { webSocket.cleanupClients(); }

        bool attemptNewConnection(BaseRemoteServerConnection *remoteConnection) override;

        AsyncWebSocket *getWebSocketHandler() { return &webSocket; }
    };

    /**
     * Contains the components of a single connection, which is essentially the remoteConnector and the transport, this
     * allows us to send and receive messages on that transport, connect to it, and determine if it is still connected.
     * It also allows us to establish new connections via the transport.
     */
    class EspWebSocketRemoteConnection : public BaseRemoteServerConnection {
    private:
        TagValueRemoteConnector remoteConnector;
        EspAsyncWebSocketClient remoteTransport;
        CombinedMessageProcessor messageProcessor;
    public:
        explicit EspWebSocketRemoteConnection(EspAsyncWebserver &webServer);

        void init(int remoteNumber, const ConnectorLocalInfo &info) override;

        TagValueRemoteConnector *connector() { return &remoteConnector; }

        EspAsyncWebSocketClient *transport() { return &remoteTransport; }

        CombinedMessageProcessor *messageProcessors() { return &messageProcessor; }

        void tick() override;

        bool connected() override { return remoteTransport.connected(); }

        void copyConnectionStatus(char *buffer, int bufferSize) override;
    };

    int fromWiFiRSSITo4StateIndicator(int strength);
}

#ifndef TC_MANUAL_NAMESPACING
using namespace tcremote;
#endif // TC_MANUAL_NAMESPACING

#endif //TCMENU_ESPASYNCWEBSERVER_H
