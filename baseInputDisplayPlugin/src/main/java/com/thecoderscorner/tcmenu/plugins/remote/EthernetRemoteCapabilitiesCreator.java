/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableBuilder;
import com.thecoderscorner.menu.pluginapi.model.FunctionCallBuilder;

import java.util.List;
import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.*;
import static com.thecoderscorner.tcmenu.plugins.remote.EthernetAdapterType.*;

public class EthernetRemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333",
                                REMOTE, TEXTUAL, uintValidator(65355)),
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device",
                                REMOTE, TEXTUAL, textValidator()),
            new CreatorProperty("LIBRARY_TYPE", "The Arduino library for your device",
                                ETHERNET_2.name(), REMOTE, TEXTUAL, choicesValidator(EthernetAdapterType.values()))
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

        EthernetAdapterType type = EthernetAdapterType.valueOf(findPropertyValue("LIBRARY_TYPE").getLatestValue());
        Map<String, String> repl;

        if(type == UIP_ENC28J60) {
            // there are some replacements that must be done for UIP Ethernet to work.
            repl = Map.of(
                    "Ethernet.h", "UIPEthernet.h",
                    "EthernetClient", "UIPClient",
                    "EthernetServer", "UIPServer"
            );
        }
        else if(type == ESP_8266) {
            // for ESP8266 we need to change the ethernet references to wifi references
            repl = Map.of(
                    "Ethernet.h", "ESP8266WiFi.h",
                    "EthernetClient", "WifiClient",
                    "EthernetServer", "WifiServer"
            );
        }
        else {
            repl = Map.of();
        }

        addLibraryFiles(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, repl),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, repl)
        );
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
