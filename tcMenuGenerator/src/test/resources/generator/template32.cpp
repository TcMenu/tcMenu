/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#include <tcMenu.h>
replacedInclude

// Global variable declarations

VarType varName(1234.34);

// Global Menu Item declarations

RENDERING_CALLBACK_NAME_INVOKE(fnIpItemRtCall, ipAddressRenderFn, "Ip Item", -1, NULL)
IpAddressMenuItem menuIpItem(fnIpItemRtCall, 79, NULL);
RENDERING_CALLBACK_NAME_INVOKE(fnTextItemRtCall, textItemRenderFn, "Text Item", -1, callback2)
TextMenuItem menuTextItem(fnTextItemRtCall, 99, 10, &menuIpItem);
const AnalogMenuInfo minfoTest2 = { "test2", 2, 4, 100, callback1, 0, 1, "dB" };
AnalogMenuItem menuTest2(&minfoTest2, 0, &menuTextItem);
RENDERING_CALLBACK_NAME_INVOKE(fnSubRtCall, backSubItemRenderFn, "sub", -1, NULL)
const SubMenuInfo minfoSub = { "sub", 100, 0xffff, 0, NO_CALLBACK };
BackMenuItem menuBackSub(fnSubRtCall, &menuTest2);
SubMenuItem menuSub(&minfoSub, &menuBackSub, NULL);
ListRuntimeMenuItem menuAbc(1043, 2, fnAbcRtCall, &menuSub);
const AnalogMenuInfo minfoTest = { "test", 1, 2, 100, NO_CALLBACK, 0, 1, "dB" };
AnalogMenuItem menuTest(&minfoTest, 0, &menuAbc);
const char enumStrExtra_0[] = "test";
const char* const enumStrExtra[]  = { enumStrExtra_0 };
const EnumMenuInfo minfoExtra = { "Extra", 20, 5, 0, NO_CALLBACK, enumStrExtra };
EnumMenuItem menuExtra(&minfoExtra, 0, &menuTest);
const ConnectorLocalInfo applicationInfo = { "tester", "uuid1" };

// Set up code

void setupMenu() {
    lcd.begin(16, 2);

    // Read only and local only function calls
    menuTest2.setReadOnly(true);
    menuTest.setReadOnly(true);
    menuSub.setLocalOnly(true);
    menuTest2.setLocalOnly(true);
}

