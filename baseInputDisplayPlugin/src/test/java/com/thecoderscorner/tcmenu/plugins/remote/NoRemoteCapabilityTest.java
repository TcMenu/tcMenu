package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NoRemoteCapabilityTest {

    @Test
    public void testNoRemoteCapabilities() {
        EmbeddedCodeCreator creator = new NoRemoteCapability();
        assertEquals(1, creator.properties().size());

        findAndSetValueOnProperty(creator, "DEVICE_NAME", REMOTE, CreatorProperty.PropType.TEXTUAL, "Tester");
        ((NoRemoteCapability) creator).initCreator("root");

        assertThat("const char PROGMEM applicationName[] = \"Tester\";\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());
        assertThat(creator.getSetupCode("root")).isBlank();
        assertThat("extern const char applicationName[];\n").isEqualToIgnoringNewLines(creator.getExportDefinitions());
        assertThat(creator.getIncludes()).containsExactly("#include \"RemoteConnector.h\"");
        assertThat(creator.getRequiredFiles()).isEmpty();
    }
}