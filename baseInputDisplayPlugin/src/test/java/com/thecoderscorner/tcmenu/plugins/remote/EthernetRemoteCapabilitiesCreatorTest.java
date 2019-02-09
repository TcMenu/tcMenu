package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
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

        assertThat("const char PROGMEM applicationName[] = \"Tester\";\n" +
                                         "EthernetServer server(3333);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    remoteServer.begin(&server, applicationName);\n")
                .isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat("extern const char applicationName[];\n")
                .isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat(creator.getIncludes()).containsExactlyInAnyOrder(
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\"");

        assertThat(creator.getRequiredFiles()).containsExactly(
                "remotes/ethernet/EthernetTransport.cpp",
                "remotes/ethernet/EthernetTransport.h");
    }
}