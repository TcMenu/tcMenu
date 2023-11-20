/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#include <tcMenu.h>
#include "project_menu.h"
#include <Fonts/sans24p7b.h>

// Global variable declarations
const  ConnectorLocalInfo applicationInfo = { "app", "4490f2fb-a48b-4c89-b6e5-7f557e5f6faf" };
AvrEeprom glAvrRom;
EepromAuthenticatorManager authManager(3);
ArduinoAnalogDevice analogDevice(42);
const int anotherVar;
const int allowedPluginVar;

// Global Menu Item declarations
const AnyMenuInfo minfoOverrideSubNameIpItem = { "Ip Item", 79, 0xffff, 0, headerOnly };
IpAddressMenuItem menuOverrideSubNameIpItem(&minfoOverrideSubNameIpItem, IpAddressStorage(127, 0, 0, 1), nullptr, INFO_LOCATION_PGM);
const AnyMenuInfo minfoOverrideSubNameTextItem = { "Text Item", 99, 0xffff, 0, callback2 };
TextMenuItem menuOverrideSubNameTextItem(&minfoOverrideSubNameTextItem, "", 10, &menuOverrideSubNameIpItem, INFO_LOCATION_PGM);
const AnalogMenuInfo minfoOverrideAnalog2Name = { "test2", 2, 4, 100, callback1, 0, 1, "dB" };
AnalogMenuItem menuOverrideAnalog2Name(&minfoOverrideAnalog2Name, 0, &menuOverrideSubNameTextItem, INFO_LOCATION_PGM);
const SubMenuInfo minfoOverrideSubName = { "sub", 100, 0xffff, 0, NO_CALLBACK };
BackMenuItem menuBackOverrideSubName(&minfoOverrideSubName, &menuOverrideAnalog2Name, INFO_LOCATION_PGM);
SubMenuItem menuOverrideSubName(&minfoOverrideSubName, &menuBackOverrideSubName, nullptr, INFO_LOCATION_PGM);
const AnyMenuInfo minfoAbc = { "Abc", 1043, 0, 0, NO_CALLBACK };
ListRuntimeMenuItem menuAbc(&minfoAbc, 2, fnAbcRtCall, &menuOverrideSubName, INFO_LOCATION_PGM);
const AnalogMenuInfo minfoTest = { "test", 1, 2, 100, NO_CALLBACK, 0, 1, "dB" };
AnalogMenuItem menuTest(&minfoTest, 0, &menuAbc, INFO_LOCATION_PGM);
const char enumStrExtra_0[] = "test";
const char* const enumStrExtra[]  = { enumStrExtra_0 };
const EnumMenuInfo minfoExtra = { "Extra", 20, 5, 0, callback1, enumStrExtra };
EnumMenuItem menuExtra(&minfoExtra, 0, &menuTest, INFO_LOCATION_PGM);

void setupMenu() {
    // First we set up eeprom and authentication (if needed).
    setSizeBasedEEPROMStorageEnabled(true);
    menuMgr.setEepromRef(&glAvrRom);
    authManager.initialise(menuMgr.getEepromAbstraction(), 100);
    menuMgr.setAuthenticator(&authManager);
    // Now add any readonly, non-remote and visible flags.
    menuOverrideAnalog2Name.setReadOnly(true);
    menuTest.setReadOnly(true);
    menuOverrideAnalog2Name.setLocalOnly(true);
    menuOverrideSubName.setLocalOnly(true);

    // Code generated by plugins and new operators.
    switches.initialise(io23017, true, MenuFontDef(&sans24p7b, 1), internalDigitalIo());
    switches.addSwitch(BUTTON_PIN, null);
    switches.onRelease(BUTTON_PIN, [](uint8_t /*key*/, bool held) {
            anotherFn(20);
        });
}

