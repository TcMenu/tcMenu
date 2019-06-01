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
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.choicesValidator;
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.uintValidator;
import static com.thecoderscorner.tcmenu.plugins.remote.EthernetAdapterType.ETHERNET_2;
import static com.thecoderscorner.tcmenu.plugins.remote.EthernetAdapterType.UIP_ENC28J60;

public class EthernetRemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333",
                                REMOTE, TEXTUAL, uintValidator(65355)),
            new CreatorProperty("LIBRARY_TYPE", "The Arduino library for your device",
                                ETHERNET_2.name(), REMOTE, TEXTUAL, choicesValidator(EthernetAdapterType.values()))
    );


    @Override
    protected void initCreator(String root) {

        addVariable(new CodeVariableBuilder().variableType("EthernetServer").variableName("server")
                            .paramFromPropertyWithDefault("LISTEN_PORT", "3333")
                            .requiresHeader("EthernetTransport.h", true)
                            .requiresHeader("RemoteConnector.h", false)
        );

        addFunctionCall(new FunctionCallBuilder().objectName("remoteServer").functionName("begin")
                       .paramRef("server").paramRef("applicationInfo"));

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
