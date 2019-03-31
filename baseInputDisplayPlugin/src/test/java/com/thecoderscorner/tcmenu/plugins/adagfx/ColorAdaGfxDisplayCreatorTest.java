/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.adagfx;

import com.thecoderscorner.menu.pluginapi.PluginFileDependency;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.tcmenu.plugins.util.TestUtil;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;

class ColorAdaGfxDisplayCreatorTest {
    @Test
    public void testAdaGfxCreator() {
        ColorAdaGfxDisplayCreator creator = new ColorAdaGfxDisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_WIDTH", SubSystem.DISPLAY, USE_IN_DEFINE, "320");
        findAndSetValueOnProperty(creator, "DISPLAY_HEIGHT", SubSystem.DISPLAY, USE_IN_DEFINE, "240");
        findAndSetValueOnProperty(creator, "DISPLAY_ROTATION", SubSystem.DISPLAY, USE_IN_DEFINE, "0");
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEqualToIgnoringNewLines(
                "#define DISPLAY_WIDTH 320\n" +
                        "#define DISPLAY_HEIGHT 240\n" +
                        "#define DISPLAY_ROTATION 0"
        );
        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern Adafruit_ILI9341 gfx;\n" +
                        "extern AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "AdaColorGfxMenuConfig gfxConfig;\n" +
                        "AdaFruitGfxMenuRenderer renderer(DISPLAY_WIDTH, DISPLAY_HEIGHT);"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    prepareAdaColorDefaultGfxConfig(&gfxConfig);\n" +
                        "    gfx.begin();\n" +
                        "    gfx.setRotation(0);\n" +
                        "    renderer.setGraphicsDevice(&gfx, &gfxConfig);"
        );

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("adaGfxColor/tcMenuAdaFruitGfx.cpp", WITH_PLUGIN, Map.of()),
                new PluginFileDependency("adaGfxColor/tcMenuAdaFruitGfx.h", WITH_PLUGIN, Map.of())
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuAdaFruitGfx.h\"", "#include <Adafruit_ILI9341.h>"
        );

    }
}