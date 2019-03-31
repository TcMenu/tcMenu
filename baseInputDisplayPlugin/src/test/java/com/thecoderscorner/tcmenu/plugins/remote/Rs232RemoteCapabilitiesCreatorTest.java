/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.remote;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
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
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isBlank();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern const char applicationName[];"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "const char PROGMEM applicationName[] = \"Tester\";\n"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    remoteServer.begin(&Serial, applicationName);\n"
        );

        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include <RemoteConnector.h>",
                "#include \"SerialTransport.h\""
        );

        assertThat(creator.getRequiredFiles()).containsExactly(
                new PluginFileDependency("serialSrc/SerialTransport.cpp", WITH_PLUGIN, Map.of()),
                new PluginFileDependency("serialSrc/SerialTransport.h", WITH_PLUGIN, Map.of())
        );
    }
}