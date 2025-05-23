/*
    The code in this file uses open source libraries provided by thecoderscorner

    DO NOT EDIT THIS FILE, IT WILL BE GENERATED EVERY TIME YOU USE THE UI DESIGNER
    INSTEAD EITHER PUT CODE IN YOUR SKETCH OR CREATE ANOTHER SOURCE FILE.

    All the variables you may need access to are marked extern in this file for easy
    use elsewhere.
 */

#ifndef MENU_GENERATED_CODE_H
#define MENU_GENERATED_CODE_H

#include <Arduino.h>
#include <tcMenu.h>
#include "Scramble.h"
#include <JoystickSwitchInput.h>
#include <RuntimeMenuItem.h>
#include <IoAbstraction.h>
#include <EepromItemStorage.h>
#include <EepromAbstraction.h>
#include <RemoteAuthentication.h>

// variables we declare that you may need to access
extern const PROGMEM ConnectorLocalInfo applicationInfo;
extern ArduinoAnalogDevice analogDevice;
extern char[] expOnly;
extern const GFXfont sans24p7b;

// Any externals needed by IO expanders, EEPROMs etc
extern IoAbstractionRef 123;

// Global Menu Item exports
extern IpAddressMenuItem menuIpItem;
extern TextMenuItem menuTextItem;
extern AnalogMenuItem menuOverrideAnalog2Name;
extern BackMenuItem menuBackOverrideSubName;
extern SubMenuItem menuOverrideSubName;
extern ListRuntimeMenuItem menuAbc;
extern AnalogMenuItem menuTest;
extern EnumMenuItem menuExtra;

// Provide a wrapper to get hold of the root menu item and export setupMenu
inline MenuItem& rootMenuItem() { return menuExtra; }
void setupMenu();

// Callback functions must always include CALLBACK_FUNCTION after the return type
#define CALLBACK_FUNCTION

void CALLBACK_FUNCTION callback1(int id);
void CALLBACK_FUNCTION callback2(int id);
int fnAbcRtCall(RuntimeMenuItem* item, uint8_t row, RenderFnMode mode, char* buffer, int bufferSize);
void CALLBACK_FUNCTION headerOnly(int id);

#endif // MENU_GENERATED_CODE_H
