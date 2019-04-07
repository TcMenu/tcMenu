/**
 * The purpose of this file is purely to test the Ethernet transport in place, it is not designed as an example
 */

#include <Ethernet.h>
#include <tcMenu.h>
#include "EthernetTransport.h"
#include "tcm_test/testFixtures.h"
//#include "utility/logging.h"

const char applicationName[] PROGMEM = "Testdevice";
NoRenderer noRenderer;

byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
IPAddress ip(192, 168, 0, 96);
EthernetServer server(3333);

void onCommsNotify(CommunicationInfo status) {
    serdebugF4("Comms notify: (rNo, connected, errNo)", status.remoteNo, status.connected, status.errorMode);
}
 
void setup() {
    while(!Serial);
    Serial.begin(115200);

    Ethernet.begin(mac, ip);

    serdebugF("started ethernet");

    menuMgr.initWithoutInput(&noRenderer, &menuVolume);
    remoteServer.begin(&server, applicationName);
    remoteServer.getRemoteConnector(0)->setCommsNotificationCallback(onCommsNotify);

    serdebugF("started app");

    taskManager.scheduleFixedRate(150, []{
        cycleAnalogMenu(menuVolume);
        cycleAnalogMenu(menuLHSTemp);
        cycleAnalogMenu(menuRHSTemp);
        cycleAnalogMenu(menuContrast);
    });
}

void cycleAnalogMenu(AnalogMenuItem& item) {
        unsigned int vol = item.getCurrentValue();
        vol++;
        if(vol > item.getMaximumValue()) vol = 0;
        item.setCurrentValue(vol);

}

void loop() {
    taskManager.runLoop();
    Ethernet.maintain();
}