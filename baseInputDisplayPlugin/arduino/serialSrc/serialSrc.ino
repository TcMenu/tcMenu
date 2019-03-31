/**
 * The purpose of this file is purely to test the Ethernet transport in place, it is not designed as an example
 */

#include <tcMenu.h>
#include "SerialTransport.h"
#include "tcm_test/testFixtures.h"
#include <IoLogging.h>

const char applicationName[] = "Testdevice";
NoRenderer noRenderer;

void onCommsNotify(CommsNotificationType status) {
    serdebugF2("Comms notify: ", status);
}
 
void setup() {
    while(!Serial);
    Serial.begin(115200);

    TagValueTransport::setNoificationFn(onCommsNotify);

    menuMgr.initWithoutInput(&noRenderer, &menuVolume);
    remoteServer.begin(&Serial, applicationName);

    menuLHSTemp.setReadOnly(true);

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
