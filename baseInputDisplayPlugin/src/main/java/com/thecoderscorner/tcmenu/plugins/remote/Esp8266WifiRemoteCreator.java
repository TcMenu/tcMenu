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

public class Esp8266WifiRemoteCreator extends AbstractCodeCreator {
    public enum EspProcessorType { ESP_8266, ESP_32 };
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333",
                    REMOTE, TEXTUAL, uintValidator(65355)),
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device",
                    REMOTE, TEXTUAL, textValidator()),
            new CreatorProperty("PROCESSOR_TYPE", "Choose the type of ESP processor in the board",
                                EspProcessorType.ESP_8266.name(),
                                REMOTE, TEXTUAL, choicesValidator(EspProcessorType.values()))
    );

    @Override
    protected void initCreator(String root) {
        var deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();
        var isEsp32 = findPropertyValue("PROCESSOR_TYPE").getLatestValue().equals(EspProcessorType.ESP_32.name());
        var wifiInclude = isEsp32 ? "WiFi.h" : "ESP8266WiFi.h";

        addVariable(new CodeVariableBuilder().variableName("applicationName[]").variableType("char")
                .quoted(deviceName).progmem().byAssignment().exportNeeded()
                .requiresHeader("RemoteConnector.h", false));

        addVariable(new CodeVariableBuilder().variableType("WiFiServer").variableName("server")
                .paramFromPropertyWithDefault("LISTEN_PORT", "3333")
                .requiresHeader(wifiInclude, false));

        addFunctionCall(new FunctionCallBuilder().objectName("remoteServer").functionName("begin")
                .paramRef("server").param("applicationName")
                .requiresHeader("EthernetTransport.h", true));

        var repl = Map.of(
                "Ethernet.h", wifiInclude,
                "EthernetClient", "WiFiClient",
                "EthernetServer", "WiFiServer"
        );

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
