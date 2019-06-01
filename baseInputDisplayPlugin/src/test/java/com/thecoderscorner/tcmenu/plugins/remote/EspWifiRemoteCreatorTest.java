/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EspWifiRemoteCreatorTest {
    @Test
    void testEsp8266Creation() {
        Esp8266WifiRemoteCreator creator = new Esp8266WifiRemoteCreator();
        assertEquals(1, creator.properties().size());
        findAndSetValueOnProperty(creator, "LISTEN_PORT", REMOTE, CreatorProperty.PropType.TEXTUAL, "3333");

        var extractor = TestUtil.extractorFor(creator);

        creator.initCreator("root");

        assertStandardFields(creator, extractor);

        var repl = Map.of(
                "Ethernet.h", "ESP8266WiFi.h",
                "EthernetClient", "WiFiClient",
                "EthernetServer", "WiFiServer"
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, repl),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, repl)
        );

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <ESP8266WiFi.h>",
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );
    }

    @Test
    void testEsp32Creation() {
        Esp32WifiRemoteCreator creator = new Esp32WifiRemoteCreator();
        assertEquals(1, creator.properties().size());

        findAndSetValueOnProperty(creator, "LISTEN_PORT", REMOTE, CreatorProperty.PropType.TEXTUAL, "3333");
        var extractor = TestUtil.extractorFor(creator);

        creator.initCreator("root");

        assertStandardFields(creator, extractor);

        var repl = Map.of(
                "Ethernet.h", "WiFi.h",
                "EthernetClient", "WiFiClient",
                "EthernetServer", "WiFiServer"
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, repl),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, repl)
        );

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <WiFi.h>",
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );
    }

    private void assertStandardFields(Esp8266WifiRemoteCreator creator, CodeVariableCppExtractor extractor) {
        assertThat(extractor.mapDefines()).isBlank();

        assertThat(extractor.mapExports(creator.getVariables())).isEmpty();

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "WiFiServer server(3333);"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    remoteServer.begin(&server, &applicationInfo);\n"
        );
    }
}