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
import static com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators.uintValidator;

public class Esp8266WifiRemoteCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333",
                    REMOTE, TEXTUAL, uintValidator(65355))
    );

    @Override
    protected void initCreator(String root) {
        addVariable(new CodeVariableBuilder().variableType("WiFiServer").variableName("server")
                .paramFromPropertyWithDefault("LISTEN_PORT", "3333")
                .requiresHeader(getWifiInclude(), false));

        addFunctionCall(new FunctionCallBuilder().objectName("remoteServer").functionName("begin")
                .paramRef("server").paramRef("applicationInfo")
                .requiresHeader("EthernetTransport.h", true)
                .requiresHeader("RemoteConnector.h", false));

        var repl = Map.of(
                "Ethernet.h", getWifiInclude(),
                "EthernetClient", "WiFiClient",
                "EthernetServer", "WiFiServer"
        );

        addLibraryFiles(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, repl),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, repl)
        );
    }

    protected String getWifiInclude() {
        return "ESP8266WiFi.h";
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
