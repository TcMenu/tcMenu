package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Rs232RemoteCapabilitiesCreatorTest {
    @Test
    public void testSerialRemoteCapabilities() {
        Rs232RemoteCapabilitiesCreator creator = new Rs232RemoteCapabilitiesCreator();
        assertEquals(2, creator.properties().size());
        findAndSetValueOnProperty(creator, "DEVICE_NAME", REMOTE, CreatorProperty.PropType.TEXTUAL, "Tester");
        findAndSetValueOnProperty(creator, "SERIAL_PORT", REMOTE, CreatorProperty.PropType.VARIABLE, "Serial");
        creator.initCreator("root");

        assertThat("const char PROGMEM applicationName[] = \"Tester\";\n").isEqualToIgnoringNewLines(creator.getGlobalVariables());
        assertThat("    remoteServer.begin(&Serial, applicationName);\n").isEqualToIgnoringNewLines(creator.getSetupCode("root"));
        assertThat("\nextern const char applicationName[];\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());
        assertThat(creator.getIncludes()).containsExactlyInAnyOrder(
                "#include <RemoteConnector.h>",
                "#include \"SerialTransport.h\"");
        assertThat(creator.getRequiredFiles()).containsExactly(
                "remotes/serial/SerialTransport.cpp",
                "remotes/serial/SerialTransport.h");
    }
}