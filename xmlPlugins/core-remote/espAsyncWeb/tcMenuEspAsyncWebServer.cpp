/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * ESP Async Webserver remote capability plugin. This file is a plugin file and should not be directly edited,
 * it will be replaced each time the project is built. If you want to edit this file in place,
 * make sure to rename it first.
 */

#include "tcMenuEspAsyncWebServer.h"
#include <ExecWithParameter.h>
#include <SCCircularBuffer.h>

using namespace tcremote;
using namespace tccollection;

extern TcMenuRemoteServer remoteServer;

const char TAG_TC_WEBSERVER[] = "tcweb";


EspAsyncWebSocketClient *findRemoteServer(uint32_t id) {
    for (auto i = 0; i < remoteServer.remoteCount(); ++i) {
        BaseRemoteServerConnection *con = remoteServer.getRemoteServerConnection(i);
        if (con != nullptr && con->connected() && con->getRemoteServerType() == TAG_VAL_WEB_SOCKET) {
            auto *wsCon = reinterpret_cast<EspWebSocketRemoteConnection *>(con);
            if(wsCon->transport()->getClientId() == id) {
                return wsCon->transport();
            }
        }
    }
    return nullptr;
}

void onWebSockEvent(AsyncWebSocket *server, AsyncWebSocketClient *client, AwsEventType type, void *arg, uint8_t *data, size_t len) {
    ESP_LOGI(TAG_TC_WEBSERVER, "Start Con");
    if (type == WS_EVT_CONNECT) {
        taskManager.execute(new ExecWithParameter<uint32_t>([](uint32_t id) {
            for (int i = 0; i < remoteServer.remoteCount(); ++i) {
                auto connector = remoteServer.getRemoteServerConnection(i);
                if (connector->getRemoteServerType() == TAG_VAL_WEB_SOCKET && !connector->connected()) {
                    auto *wsCon = reinterpret_cast<EspWebSocketRemoteConnection *>(connector);
                    wsCon->transport()->registerConnection(id);
                    ESP_LOGI(TAG_TC_WEBSERVER, "End Con success %d", id);
                    return;
                }
            }
        }, client->id()), true);
        ESP_LOGI(TAG_TC_WEBSERVER, "End Con too many");
    } else if (type == WS_EVT_DISCONNECT || type == WS_EVT_ERROR) {
        ESP_LOGI(TAG_TC_WEBSERVER, "Start ERR");
        taskManager.execute(new ExecWithParameter<uint32_t>([](uint32_t id){
            EspAsyncWebSocketClient *cl = findRemoteServer(id);
            if (cl) cl->markNotInUse();
        }, client->id()), true);
        ESP_LOGI(TAG_TC_WEBSERVER, "End ERR");
    } else if (type == WS_EVT_PONG) {
        //pong message was received (in response to a ping request maybe)
    } else if (type == WS_EVT_DATA) {
        ESP_LOGI(TAG_TC_WEBSERVER, "Start Data rx");
        auto *frameInfo = reinterpret_cast<AwsFrameInfo *>(arg);
        if (frameInfo->len == len && frameInfo->final) {
            EspAsyncWebSocketClient *cl = findRemoteServer(client->id());
            if (cl && cl->dataAvailable(data, len)) {
                taskManager.execute(cl);
            }
        }
        ESP_LOGI(TAG_TC_WEBSERVER, "End Data rx");
    }
}

EspAsyncWebserver::EspAsyncWebserver(const char *webSocketAddress, int port)
        : webServer(port), webSocket(webSocketAddress) {}

bool EspAsyncWebserver::attemptInitialisation() {
    if(!WiFi.isConnected()) return false;
    serdebugF("Calling prepareWebServer");
    prepareWebServer(webServer);
    serdebugF("Initialise web sockets");
    webSocket.onEvent(onWebSockEvent);
    webServer.addHandler(&webSocket);
    serdebugF("Fully initialised web server");
    webServer.begin();
    initialised = true;

    taskManager.scheduleFixedRate(1, this, TIME_SECONDS);
    return true;
}

bool EspAsyncWebserver::attemptNewConnection(BaseRemoteServerConnection *remoteConnection) {
    webSocket.cleanupClients();
    // this is an entirely async library, we are told when there is a connection.
    return false;
}

EspWebSocketRemoteConnection::EspWebSocketRemoteConnection(EspAsyncWebserver& webServer)
        : BaseRemoteServerConnection(webServer, TAG_VAL_WEB_SOCKET),
          remoteTransport(webServer.getWebSocketHandler()),
          messageProcessor() { }

void EspWebSocketRemoteConnection::init(int remoteNumber, const ConnectorLocalInfo &info) {
    // first we setup the remote number and initialise the connector
    connector()->initialise(transport(), messageProcessors(), &info, remoteNumber);
}

void EspWebSocketRemoteConnection::copyConnectionStatus(char *buffer, int bufferSize) {
    strcpy(buffer, "WS ");
    if(connected()) {
        strncat(buffer, remoteConnector.getRemoteName(), bufferSize);
    }
    else {
        strncat(buffer, "Closed", bufferSize);
    }
    buffer[bufferSize - 1] = 0;
}

void EspWebSocketRemoteConnection::tick() {
    remoteConnector.tick();

    // if this is a buffered transport, we must give it chance to flush the buffer from time to time.
    if(remoteTransport.getTransportType() == TVAL_BUFFERED) {
        reinterpret_cast<BaseBufferedRemoteTransport&>(remoteTransport).flushIfRequired();
    }
}

EspAsyncWebSocketClient::EspAsyncWebSocketClient(AsyncWebSocket *webSock)
        : BaseBufferedRemoteTransport(BUFFER_MESSAGES_TILL_FULL, 128, 128),
          webSocket(webSock), wsClientId((uint32_t) -1), inUse(false), remoteNum(0) {
}

void EspAsyncWebSocketClient::flush() {
    auto clientId = wsClientId;
    if(clientId == -1 || writeBufferPos == 0) return;
    if (webSocket->availableForWrite(clientId)) {
        serdebugF3("Flush WS ", clientId, writeBufferPos);
        //serdebugHexDump(" data=", writeBuffer, writeBufferPos);
        webSocket->text(clientId, writeBuffer, writeBufferPos);
        writeBufferPos = 0;
    }
    else {
        serdebugF2("Client unavailable ", clientId);
    }
}

bool EspAsyncWebSocketClient::available() {
    auto clientId = wsClientId;
    return inUse && clientId != -1 && webSocket->availableForWrite(clientId);
}

void EspAsyncWebSocketClient::close() {
    serdebugF2("Close WS ", wsClientId);
    BaseBufferedRemoteTransport::close();

    //webSocket->close(wsClientId);
    markNotInUse();
}

void EspAsyncWebSocketClient::markNotInUse() {
    wsClientId = (uint32_t) -1;
    inUse = false;
    writeBufferPos = 0;
    readBufferPos = 0;
    readBufferAvail = 0;
    currentField.msgType = UNKNOWN_MSG_TYPE;
    currentField.fieldType = FVAL_PROCESSING_AWAITINGMSG;
}

bool EspAsyncWebSocketClient::dataAvailable(uint8_t *data, size_t len) {
    for(size_t i=0;i<len;i++) {
        readThreadBuffer.put(data[i]);
    }
    return true;
}

void EspAsyncWebSocketClient::registerConnection(uint32_t id) {
    inUse = true;
    wsClientId = id;
}

void EspAsyncWebSocketClient::exec() {
    //serdebugHexDump("data in", (const void*)readThreadBuffer, readThreadLength);

    while(readThreadBuffer.available() && readBufferAvail < readBufferSize) {
        readBuffer[readBufferAvail] = readThreadBuffer.get();
        ++readBufferAvail;
    }
}

int tcremote::fromWiFiRSSITo4StateIndicator(int strength) {
    int qualityIcon = 0;
    if(strength > -50) qualityIcon = 4;
    else if(strength > -60) qualityIcon = 3;
    else if(strength > -75) qualityIcon = 2;
    else if(strength > -90) qualityIcon = 1;
    return qualityIcon;
}
