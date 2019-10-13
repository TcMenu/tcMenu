/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.List;
import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.boolValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.variableValidator;

public class ESPSerialRemoteCapabilitiesCreator extends Rs232RemoteCapabilitiesCreator {
    private final List<CreatorProperty> espCreatorProperties = List.of(
            new CreatorProperty("SERIAL_PORT", "Serial port variable name", "Serial",
                    REMOTE, VARIABLE, variableValidator()),
            new CreatorProperty("HARDWARE_PORT", "Port is the default, IE: HardwareSerial",
                    "true", REMOTE, VARIABLE, boolValidator())
    );


    public Map<String, String> getReplacementMap() {
        var isHardware = getBooleanFromProperty("HARDWARE_PORT");

        // ESP arduino implementation only has availableForWrite on the HardwareSerial
        // class and not on stream. We need to change behaviour appropriately
        if(isHardware) return Map.of("Stream\\*", "HardwareSerial*");
        else return Map.of("serialPort->availableForWrite() != 0", "true");
    }

    @Override
    public List<CreatorProperty> properties() {
        return espCreatorProperties;
    }
}
