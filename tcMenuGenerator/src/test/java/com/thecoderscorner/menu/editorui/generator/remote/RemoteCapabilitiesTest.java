package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.REMOTE;
import static com.thecoderscorner.menu.editorui.util.TestUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RemoteCapabilitiesTest {
    @Test
    public void testRemoteCapabilitiesValues() {
        assertEquals(3, RemoteCapabilities.values.size());
        assertEquals("No Remote", RemoteCapabilities.values.get(1).getDescription());
        assertTrue(RemoteCapabilities.values.get(1).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("Serial Remote", RemoteCapabilities.values.get(2).getDescription());
        assertTrue(RemoteCapabilities.values.get(2).isApplicableFor(EmbeddedPlatform.ARDUINO));

        assertEquals("Ethernet Remote", RemoteCapabilities.values.get(3).getDescription());
        assertTrue(RemoteCapabilities.values.get(3).isApplicableFor(EmbeddedPlatform.ARDUINO));
    }

    @Test
    public void testNoRemoteCapabilities() {
        EmbeddedCodeCreator creator = RemoteCapabilities.values.get(1).makeCreator(makeEditorProject());
        assertEquals(1, creator.properties().size());

        findAndCheckProperty(creator, "DEVICE_NAME", REMOTE, PropType.TEXTUAL, "Tester");

        assertEqualsIgnoringCRLF("const char PROGMEM applicationName[] = \"Tester\";\n", creator.getGlobalVariables());
        assertEqualsIgnoringCRLF("", creator.getSetupCode("root"));
        assertEqualsIgnoringCRLF("extern const char applicationName[];\n", creator.getExportDefinitions());
        assertThat(creator.getIncludes()).containsExactly("#include \"RemoteConnector.h\"");
        assertThat(creator.getRequiredFiles()).isEmpty();
    }

    @Test
    public void testSerialRemoteCapabilities() {
        EmbeddedCodeCreator creator = RemoteCapabilities.values.get(2).makeCreator(makeEditorProject());
        assertEquals(2, creator.properties().size());
        findAndCheckProperty(creator, "DEVICE_NAME", REMOTE, PropType.TEXTUAL, "Tester");
        findAndCheckProperty(creator, "SERIAL_PORT", REMOTE, PropType.VARIABLE, "Serial");

        assertEqualsIgnoringCRLF("const char PROGMEM applicationName[] = \"Tester\";\n", creator.getGlobalVariables());
        assertEqualsIgnoringCRLF("    remoteServer.begin(&Serial, applicationName);\n", creator.getSetupCode("root"));
        assertEqualsIgnoringCRLF("\nextern const char applicationName[];\n", creator.getExportDefinitions());
        assertThat(creator.getIncludes()).containsExactly(
                "#include <RemoteConnector.h>",
                "#include \"SerialTransport.h\""
        );
        assertThat(creator.getRequiredFiles()).containsExactly(
                "remotes/serial/SerialTransport.cpp",
                "remotes/serial/SerialTransport.h"
        );
    }

    @Test
    public void testEthernetRemoteCapabilites() {
        EmbeddedCodeCreator creator = RemoteCapabilities.values.get(3).makeCreator(makeEditorProject());
        assertEquals(2, creator.properties().size());

        findAndCheckProperty(creator, "DEVICE_NAME", REMOTE, PropType.TEXTUAL, "Tester");
        findAndCheckProperty(creator, "LISTEN_PORT", REMOTE, PropType.USE_IN_DEFINE, "3333");

        assertEqualsIgnoringCRLF("const char PROGMEM applicationName[] = \"Tester\";\n" +
                "EthernetServer server(LISTEN_PORT);\n", creator.getGlobalVariables());

        assertEqualsIgnoringCRLF("    remoteServer.begin(&server, applicationName);\n",
                creator.getSetupCode("root"));

        assertEqualsIgnoringCRLF("#define LISTEN_PORT 3333\n" +
                "extern const char applicationName[];\n", creator.getExportDefinitions());

        assertThat(creator.getIncludes()).containsExactly(
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                "remotes/ethernet/EthernetTransport.cpp",
                "remotes/ethernet/EthernetTransport.h"
        );
    }
}