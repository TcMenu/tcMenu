/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

/**
 * SimHub Connector  that connects to simhub using the serial port and changes menu items based on simhub update.
 * This class should not be directly edited, it will be replaced each time the project is built.
 * If you want to edit this file in place, make sure to rename it first.
 */

#ifndef TCLIBRARYDEV_SIMHUBCONNECTOR_H
#define TCLIBRARYDEV_SIMHUBCONNECTOR_H

/**
 * this is the maximum number of chars on a line
 */
#define MAX_LINE_WIDTH 32

#include <Arduino.h>
#include <TaskManager.h>
#include <HardwareSerial.h>
#include <IoLogging.h>

#if defined(ESP8266) || defined(ESP32)
# define SerPortName HardwareSerial
#else
# define SerPortName Stream
#endif

class SimhubConnector : public Executable {
private:
    SerPortName* serialPort;
    BooleanMenuItem* statusMenuItem;
    char lineBuffer[MAX_LINE_WIDTH];
    int linePosition;
public:
    SimhubConnector();
    void begin(SerPortName* serialPort, int statusMenuId);

    void exec() override;
private:
    void processCommandFromSimhub();
    void processTcMenuCommand();
    void changeStatus(bool b);
};


#endif //TCLIBRARYDEV_SIMHUBCONNECTOR_H
