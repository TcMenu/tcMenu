/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class EthernetRemoteCapabilitiesCreatorTest {
    @Test
    public void testEthernetRemoteCapabilites() {
        EthernetRemoteCapabilitiesCreator creator = new EthernetRemoteCapabilitiesCreator();
        assertEquals(2, creator.properties().size());

        findAndSetValueOnProperty(creator, "DEVICE_NAME", REMOTE, PropType.TEXTUAL, "Tester");
        findAndSetValueOnProperty(creator, "LISTEN_PORT", REMOTE, PropType.TEXTUAL, "3333");
        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isBlank();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern const char applicationName[];"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "const char PROGMEM applicationName[] = \"Tester\";\n" +
                        "EthernetServer server(3333);"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    remoteServer.begin(&server, applicationName);\n"
        );

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                "remotes/ethernet/EthernetTransport.cpp",
                "remotes/ethernet/EthernetTransport.h"
        );
    }
}