/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.textValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.uintValidator;

public class EthernetRemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333",
                                REMOTE, TEXTUAL, uintValidator(65355)),
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device",
                                REMOTE, TEXTUAL, textValidator())
    );


    @Override
    protected void initCreator(String root) {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();

        addVariable(new CodeVariableBuilder().variableName("applicationName[]").variableType("char")
                            .quoted(deviceName).progmem().byAssignment().exportNeeded()
                            .requiresHeader("RemoteConnector.h", false));

        addVariable(new CodeVariableBuilder().variableType("EthernetServer").variableName("server")
                            .paramFromPropertyWithDefault("LISTEN_PORT", "3333")
                            .requiresHeader("EthernetTransport.h", true));

        addFunctionCall(new FunctionCallBuilder().objectName("remoteServer").functionName("begin")
                       .paramRef("server").param("applicationName"));

        addLibraryFiles("remotes/ethernet/EthernetTransport.cpp","remotes/ethernet/EthernetTransport.h");
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
