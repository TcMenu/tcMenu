/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;

import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO32;
import static com.thecoderscorner.menu.pluginapi.EmbeddedPlatform.ARDUINO_AVR;
import static com.thecoderscorner.menu.pluginapi.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.pluginapi.SubSystem.INPUT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CodePluginConfigTest {
    @Test
    void testCodePlugin() {
        Gson gson = new Gson();
        InputStreamReader stream = new InputStreamReader(getClass().getResourceAsStream("/generator/code-plugin-config-test.json"));
        CodePluginConfig config = gson.fromJson(stream, CodePluginConfig.class);

        assertEquals("Unit Test Module", config.getName());
        assertEquals("https://www.apache.org/licenses/LICENSE-2.0", config.getLicenseUrl());
        assertEquals("Apache 2.0", config.getLicense());
        assertEquals("com.thecoderscorner.tcmenu.unittest", config.getModuleName());
        assertEquals("1.2.0", config.getVersion());
        assertEquals("UnitTester", config.getVendor());
        assertEquals("http://localhost", config.getVendorUrl());

        assertThat(config.getPlugins()).hasSize(2);

        EmbeddedPlatforms platforms = mock(EmbeddedPlatforms.class);
        when(platforms.getEmbeddedPlatformFromId("ARDUINO")).thenReturn(ARDUINO_AVR);
        when(platforms.getEmbeddedPlatformFromId("ARDUINO32")).thenReturn(ARDUINO32);

        CodePluginItem item = config.getPlugins().get(0);
        assertEquals("always-use-uuids", item.getId());
        assertEquals("UnitTest", item.getDescription());
        assertEquals(INPUT, item.getSubsystem());
        assertEquals("A unit test example", item.getExtendedDescription());
        assertEquals("com.thecoderscorner.tcmenu.unitest.ExamplePlugin", item.getCodeCreatorClass());
        assertEquals("example.jpg", item.getImageFileName());
        assertNull(item.getDocsLink());

        assertThat(item.getApplicability(platforms)).containsExactlyInAnyOrder(ARDUINO_AVR);

        item = config.getPlugins().get(1);
        assertEquals("always-use-uuidsII", item.getId());
        assertEquals("AnotherUnitTest", item.getDescription());
        assertEquals(DISPLAY, item.getSubsystem());
        assertEquals("example", item.getExtendedDescription());
        assertEquals("com.thecoderscorner.tcmenu.unitest.ExamplePlugin", item.getCodeCreatorClass());
        assertEquals("example.jpg", item.getImageFileName());
        assertEquals("http://localhost", item.getDocsLink());

        assertThat(item.getApplicability(platforms)).containsExactlyInAnyOrder(ARDUINO_AVR, ARDUINO32);
    }
}