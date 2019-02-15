/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.tcmenu.plugins.adagfx;

import com.thecoderscorner.menu.pluginapi.SubSystem;
import org.junit.jupiter.api.Test;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.USE_IN_DEFINE;
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

        assertThat("#define DISPLAY_WIDTH 320\n" +
                                         "#define DISPLAY_HEIGHT 240\n" +
                                         "#define DISPLAY_ROTATION 0\n" +
                                         "extern Adafruit_GFX* gfx;\n" +
                                         "extern AdaFruitGfxMenuRenderer renderer;\n")
                .isEqualToIgnoringNewLines(creator.getExportDefinitions());

        assertThat("AdaFruitGfxMenuRenderer renderer(&gfx, DISPLAY_WIDTH, DISPLAY_HEIGHT);\n")
                .isEqualToIgnoringNewLines(creator.getGlobalVariables());

        assertThat("    lcd.setRotation(0);\n").isEqualToIgnoringNewLines(creator.getSetupCode("root"));

        assertThat(creator.getRequiredFiles()).containsExactlyInAnyOrder("renderers/adafruit/tcMenuAdaFruitGfx.cpp",
                                                                         "renderers/adafruit/tcMenuAdaFruitGfx.h");
        assertThat(includeToString(creator.getIncludes())).containsExactlyInAnyOrder("#include <tcMenuAdaFruitGfx.h>");

    }
}