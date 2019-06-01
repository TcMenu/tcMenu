/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType;
import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.model.CodeVariableCppExtractor;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.remote.EthernetAdapterType.ETHERNET_2;
import static com.thecoderscorner.tcmenu.plugins.remote.EthernetAdapterType.UIP_ENC28J60;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EthernetRemoteCapabilitiesCreatorTest {
    @Test
    public void testEthernetLibrary() {
        EthernetRemoteCapabilitiesCreator creator = new EthernetRemoteCapabilitiesCreator();
        assertEquals(2, creator.properties().size());

        findAndSetValueOnProperty(creator, "LISTEN_PORT", REMOTE, PropType.TEXTUAL, "3333");
        findAndSetValueOnProperty(creator, "LIBRARY_TYPE", REMOTE, PropType.TEXTUAL, ETHERNET_2.toString());
        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertStandardFields(creator, extractor);

        assertThat(creator.getRequiredFiles()).containsExactly(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, Map.of()),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, Map.of())
        );
    }

    @Test
    public void testUipEthernetLibrary() {
        EthernetRemoteCapabilitiesCreator creator = new EthernetRemoteCapabilitiesCreator();
        assertEquals(2, creator.properties().size());

        findAndSetValueOnProperty(creator, "LISTEN_PORT", REMOTE, PropType.TEXTUAL, "3333");
        findAndSetValueOnProperty(creator, "LIBRARY_TYPE", REMOTE, PropType.TEXTUAL, UIP_ENC28J60.toString());
        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertStandardFields(creator, extractor);

        var uipRepl = Map.of(
                "Ethernet.h", "UIPEthernet.h",
                "EthernetClient", "UIPClient",
                "EthernetServer", "UIPServer"
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                new PluginFileDependency("ethernetSrc/EthernetTransport.cpp", WITH_PLUGIN, uipRepl),
                new PluginFileDependency("ethernetSrc/EthernetTransport.h", WITH_PLUGIN, uipRepl)
        );
    }

    private void assertStandardFields(EthernetRemoteCapabilitiesCreator creator, CodeVariableCppExtractor extractor) {
        assertThat(extractor.mapDefines()).isBlank();

        assertThat(extractor.mapExports(creator.getVariables())).isEmpty();

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                        "EthernetServer server(3333);"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    remoteServer.begin(&server, &applicationInfo);\n"
        );

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );
    }

}