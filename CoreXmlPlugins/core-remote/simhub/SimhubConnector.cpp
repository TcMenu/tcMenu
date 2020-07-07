/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * SimHub Connector  that connects to simhub using the serial port and changes menu items based on simhub update.
 * This class should not be directly edited, it will be replaced each time the project is built.
 * If you want to edit this file in place, make sure to rename it first.
 */

#include <MenuItems.h>
#include <MenuIterator.h>
#include <RuntimeMenuItem.h>
#include "SimhubConnector.h"

SimhubConnector::SimhubConnector() {
    this->serialPort = nullptr;
    this->linePosition = 0;
    this->statusMenuItem = nullptr;
}

void SimhubConnector::begin(HardwareSerial *serialPort, int statusMenuId) {
    this->serialPort = serialPort;
    if(statusMenuId != -1) {
        statusMenuItem = reinterpret_cast<BooleanMenuItem*>(getMenuItemById(statusMenuId));
    }
    changeStatus(false);

    if(serialPort) {
        taskManager.scheduleFixedRate(1, this);
    }
}

void SimhubConnector::exec() {
    while(serialPort->available()) {
        if(linePosition >= MAX_LINE_WIDTH) {
            lineBuffer[MAX_LINE_WIDTH - 1] = 0;
            serdebugF2("Error occurred during Rx ", lineBuffer);
            linePosition = 0;
        }

        lineBuffer[linePosition] = serialPort->read();

        if(lineBuffer[linePosition] == '\n') {
            lineBuffer[linePosition] = 0;
            serdebugF2("RX ", lineBuffer);
            // we have received a command from simhub
            processCommandFromSimhub();
            linePosition=0;
        }
        else linePosition++;
    }

}

void SimhubConnector::processCommandFromSimhub() {
    if(isDigit(lineBuffer[0])) {
        processTcMenuCommand();
    }
    else if(strcmp(lineBuffer, "simhubStart") == 0) {
        changeStatus(true);

    }
    else if(strcmp(lineBuffer, "simhubEnd") == 0) {
        changeStatus(false);
    }
}

void SimhubConnector::changeStatus(bool connected) {
    if(statusMenuItem != nullptr) {
        statusMenuItem->setBoolean(connected);
    }
}

void SimhubConnector::processTcMenuCommand() {
    int i = 0;
    char sz[6];
    while(isDigit(lineBuffer[i]) && i < 5) {
        sz[i] = lineBuffer[i];
        i++;
    };
    sz[i] = 0;
    int menuId = atoi(sz);
    while(lineBuffer[i] != '=') i++;
    char* value = &lineBuffer[i + 1];

    serdebugF4("updch, item, value", sz, menuId, value);

    MenuItem* menuItem = getMenuItemById(menuId);
    if(menuItem->getMenuType() == MENUTYPE_INT_VALUE || menuItem->getMenuType() == MENUTYPE_ENUM_VALUE || menuItem->getMenuType() == MENUTYPE_BOOLEAN_VALUE) {
        ValueMenuItem* valItem = reinterpret_cast<ValueMenuItem*>(menuItem);
        valItem->setCurrentValue(atoi(value));
    }
    else if(menuItem->getMenuType() == MENUTYPE_TEXT_VALUE) {
        TextMenuItem* txtItem = reinterpret_cast<TextMenuItem*>(menuItem);
        txtItem->setTextValue(value);
    }
}
