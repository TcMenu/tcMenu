/**
 * The purpose of this file is purely to test the Ethernet transport in place, it is not designed as an example
 */

#include <Ethernet.h>
#include <tcMenu.h>
#include "EthernetTransport.h"
#include "tcm_test/testFixtures.h"

const char applicationName[] = "Testdevice";
NoRenderer noRenderer;

byte mac[] = {
  0xDE, 0xAD, 0xBE, 0xEF, 0xFE, 0xED
};
IPAddress ip(192, 168, 0, 96);
EthernetServer server(3333);

void onCommsNotify(CommsNotificationType status) {
    serdebugF2("Comms notify: ", status);
}
 
void setup() {
    while(!Serial);
    Serial.begin(115200);

    Ethernet.begin(mac, ip);
    TagValueTransport::setNoificationFn(onCommsNotify);

    menuMgr.initWithoutInput(&noRenderer, &menuVolume);
    remoteServer.begin(&server, applicationName);

    taskManager.scheduleFixedRate(250, []{
        int vol = menuVolume.getCurrentValue();
        vol++;
        if(vol > menuVolume.getMaximumValue()) vol = 0;
        menuVolume.setCurrentValue(vol);
    });

    taskManager.scheduleFixedRate(2000, []{
        serdebugF2("12VStandby: ", menu12VStandby.getBoolean());
        serdebugF2("Contrast: ", menuContrast.getCurrentValue());
    });
}

void loop() {
    taskManager.runLoop();
}