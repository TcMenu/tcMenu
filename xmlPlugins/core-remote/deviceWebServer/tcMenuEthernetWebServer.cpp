//
// Created by dave on 18/06/2022.
//

#include "tcMenuECWebServer.h"

using namespace tcremote;

bool tcremote::areWeConnected() {
#ifdef ARDUINO_ARCH_STM32
    // we'll keep checking if the link is up before trying to initialise further
    return (Ethernet.linkStatus() != LinkOFF);
#elif defined(ESP32) || defined(ESP8266)
    return (WiFi.isConnected());
#else
    return true;
#endif
}

bool TcMenuWebSockInitialisation::attemptInitialisation() {
    if(!areWeConnected()) return false;

    serdebugF("Initialising server ");
    this->server->begin();
    initialised = true;
    return initialised;

}

bool TcMenuWebSockInitialisation::attemptNewConnection(tcremote::BaseRemoteServerConnection* remoteConnection) {
    auto client = server->available();
    if(client) {
        serdebugF("Client found");
        auto* tvCon = reinterpret_cast<TagValueRemoteServerConnection*>(remoteConnection);
        auto* wsTransport = reinterpret_cast<TcMenuWebSockTransport*>(tvCon->transport());
        wsTransport->setClient(client);
        if(performUpgradeOnClient(wsTransport)) {
            serdebugF("Transport upgraded");
            return true;
        } else {
            tvCon->transport()->close();
            return false;
        }
    } else return false;
}

AbstractWebSocketTcMenuTransport *TcMenuWebServer::attemptNewConnection() {
    if(transport.connected() && (millis() - timeStart) < 2000) return nullptr; // doing something already, cannot reconnect yet.

    EthernetClient cl = server->available();
    if(cl) {
        serdebugF("HTTPClient found");
        timeStart = millis();
        transport.setClient(cl);
        return &transport;
    }

    return nullptr;
}

TcMenuWebServer::TcMenuWebServer(EthernetServer *server) : transport(), server(server), timeStart(0) {}

void TcMenuWebServer::initialiseConnection() {
    server->begin();
}

int tcremote::fromWiFiRSSITo4StateIndicator(int strength) {
    int qualityIcon = 0;
    if(strength > -50) qualityIcon = 4;
    else if(strength > -60) qualityIcon = 3;
    else if(strength > -75) qualityIcon = 2;
    else if(strength > -90) qualityIcon = 1;
    return qualityIcon;
}

