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
import static com.thecoderscorner.menu.pluginapi.PluginFileDependency.PackagingType.WITH_PLUGIN;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.findAndSetValueOnProperty;
import static com.thecoderscorner.tcmenu.plugins.util.TestUtil.includeToString;
import static org.assertj.core.api.Assertions.assertThat;

class ColorAdaGfxDisplayCreatorTest {

    @Test
    public void testAdaGfxMono5110() {
        ColorAdaGfxDisplayCreator creator = new ColorAdaGfxDisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_BUFFERED", SubSystem.DISPLAY, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        findAndSetValueOnProperty(creator, "DISPLAY_TYPE", SubSystem.DISPLAY, TEXTUAL, "Adafruit_PCD8544");

        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEmpty();
        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern Adafruit_PCD8544 gfx;\n" +
                        "extern AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "AdaColorGfxMenuConfig gfxConfig;\n" +
                        "AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    prepareAdaMonoGfxConfigLoRes(&gfxConfig);\n" +
                        "    renderer.setGraphicsDevice(&gfx, &gfxConfig);"
        );

        var replacements = Map.of(
                "#define DISPLAY_HAS_MEMBUFFER (true|false)", "#define DISPLAY_HAS_MEMBUFFER true",
                "Adafruit_ILI9341", "Adafruit_PCD8544"
        );
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.cpp", WITH_PLUGIN, replacements),
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.h", WITH_PLUGIN, replacements)
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuAdaFruitGfx.h\"", "#include <Adafruit_PCD8544.h>", "#include <Adafruit_GFX.h>"
        );
    }

    @Test
    public void testAdaGfxOled1306() {
        ColorAdaGfxDisplayCreator creator = new ColorAdaGfxDisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_BUFFERED", SubSystem.DISPLAY, TEXTUAL, "true");
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        findAndSetValueOnProperty(creator, "DISPLAY_CONFIG", SubSystem.DISPLAY, TEXTUAL, "configFor1306");
        findAndSetValueOnProperty(creator, "DISPLAY_TYPE", SubSystem.DISPLAY, TEXTUAL, "Adafruit_SSD1306");

        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEmpty();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern AdaColorGfxMenuConfig configFor1306;\n" +
                        "extern Adafruit_SSD1306 gfx;\n" +
                        "extern AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                        "    renderer.setGraphicsDevice(&gfx, &configFor1306);"
        );

        var replacements = Map.of(
                "#define DISPLAY_HAS_MEMBUFFER (true|false)", "#define DISPLAY_HAS_MEMBUFFER true",
                "Adafruit_ILI9341", "Adafruit_SSD1306"
        );
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.cpp", WITH_PLUGIN, replacements),
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.h", WITH_PLUGIN, replacements)
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuAdaFruitGfx.h\"", "#include <Adafruit_SSD1306.h>", "#include <Adafruit_GFX.h>"
        );
    }

    @Test
    public void testAdaGfxColourIli9341() {
        ColorAdaGfxDisplayCreator creator = new ColorAdaGfxDisplayCreator();
        findAndSetValueOnProperty(creator, "DISPLAY_BUFFERED", SubSystem.DISPLAY, TEXTUAL, "false");
        findAndSetValueOnProperty(creator, "DISPLAY_VARIABLE", SubSystem.DISPLAY, TEXTUAL, "gfx");
        creator.initCreator("root");
        var extractor = TestUtil.extractorFor(creator);

        assertThat(extractor.mapDefines()).isEmpty();

        assertThat(extractor.mapExports(creator.getVariables())).isEqualToIgnoringNewLines(
                "extern Adafruit_ILI9341 gfx;\n" +
                        "extern AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapVariables(creator.getVariables())).isEqualToIgnoringNewLines(
                "AdaColorGfxMenuConfig gfxConfig;\n" +
                        "AdaFruitGfxMenuRenderer renderer;"
        );

        assertThat(extractor.mapFunctions(creator.getFunctionCalls())).isEqualToIgnoringNewLines(
                "    prepareAdaColorDefaultGfxConfig(&gfxConfig);\n" +
                        "    renderer.setGraphicsDevice(&gfx, &gfxConfig);"
        );

        var replacements = Map.of(
                "#define DISPLAY_HAS_MEMBUFFER (true|false)", "#define DISPLAY_HAS_MEMBUFFER false",
                "Adafruit_ILI9341", "Adafruit_ILI9341"
        );
        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder(
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.cpp", WITH_PLUGIN, replacements),
                new PluginFileDependency("adaGfxDriver/tcMenuAdaFruitGfx.h", WITH_PLUGIN, replacements)
        );
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder(
                "#include \"tcMenuAdaFruitGfx.h\"", "#include <Adafruit_ILI9341.h>", "#include <Adafruit_GFX.h>"
        );
    }
}
