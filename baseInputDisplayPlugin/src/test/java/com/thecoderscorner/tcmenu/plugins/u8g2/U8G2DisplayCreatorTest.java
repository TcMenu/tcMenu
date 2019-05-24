/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.u8g2;

import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;

class U8G2DisplayCreatorTest {
    @Test
    public void testU8G2OverrideConfig() {
        U8G2DisplayCreator creator = new U8G2DisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        findAndSetValueOnProperty(creator, "DISPLAY_CONFIG", SubSystem.DISPLAY, TEXTUAL, "config1");

        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEmpty();
        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern U8G2_SSD1306_128X64_NONAME_F_SW_I2C gfx;\n" +
                        "extern U8g2GfxMenuConfig config1;\n" +
                        "extern U8g2MenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "U8g2MenuRenderer renderer;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    renderer.setGraphicsDevice(&gfx, &config1);"
        );

        Map<String, String> replacements = Map.of();
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.h", WITH_PLUGIN, replacements),
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.cpp", WITH_PLUGIN, replacements)
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuU8g2.h\""
        );
    }

    @Test
    public void testU8G2NoConfig() {
        U8G2DisplayCreator creator = new U8G2DisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        findAndSetValueOnProperty(creator, "DISPLAY_CONFIG", SubSystem.DISPLAY, TEXTUAL, "");

        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEmpty();
        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern U8G2_SSD1306_128X64_NONAME_F_SW_I2C gfx;\n" +
                        "extern U8g2MenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "U8g2GfxMenuConfig gfxConfig;\n" +
                        "U8g2MenuRenderer renderer;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    prepareBasicU8x8Config(gfxConfig);\n" +
                        "    renderer.setGraphicsDevice(&gfx, &gfxConfig);"
        );

        Map<String, String> replacements = Map.of();
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.h", WITH_PLUGIN, replacements),
                new PluginFileDependency("u8g2Driver/tcMenuU8g2.cpp", WITH_PLUGIN, replacements)
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuU8g2.h\""
        );
    }

}