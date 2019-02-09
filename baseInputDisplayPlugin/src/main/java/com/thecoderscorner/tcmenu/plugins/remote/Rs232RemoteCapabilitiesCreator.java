/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.textValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.variableValidator;

public class Rs232RemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device", REMOTE, TEXTUAL, textValidator()),
            new CreatorProperty("SERIAL_PORT", "Serial port variable name", "Serial", REMOTE, VARIABLE, variableValidator())
    );


    @Override
    public void initCreator(String root) {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();
        String serialPort = findPropertyValue("SERIAL_PORT").getLatestValue();

        addFunctionCall(new FunctionCallBuilder().objectName("remoteServer").functionName("begin")
                        .requiresHeader("RemoteConnector.h", false)
                        .requiresHeader("SerialTransport.h", true)
                        .param("&" + serialPort).param("applicationName"));



        addVariable(new CodeVariableBuilder().variableName("applicationName[]").variableType("char")
                .quoted(deviceName).exportNeeded().progmem().byAssignment());

        addLibraryFiles("remotes/serial/SerialTransport.cpp","remotes/serial/SerialTransport.h");
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
