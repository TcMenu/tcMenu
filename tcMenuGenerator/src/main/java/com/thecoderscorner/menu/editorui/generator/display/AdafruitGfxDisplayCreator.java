package com.thecoderscorner.menu.editorui.generator.display;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.DISPLAY;

public class AdafruitGfxDisplayCreator extends AbstractCodeCreator {

    private List<CreatorProperty> creatorProperties = new ArrayList<>(Arrays.asList(
            new CreatorProperty("DISPLAY_VARIABLE", "The display global variable", "gfx", DISPLAY),
            new CreatorProperty("DISPLAY_WIDTH", "The display width", "320", DISPLAY),
            new CreatorProperty("DISPLAY_HEIGHT", "The display height", "240", DISPLAY)
    ));
    @Override
    public List<String> getIncludes() {
        return Arrays.asList(
                "#include <tcMenuAdaFruitGfx.h>"
        );
    }

    @Override
    public String getGlobalVariables() {
        String graphicsVar = findPropertyValue("DISPLAY_VARIABLE").getLatestValue();
        return "extern Adafruit_GFX " + graphicsVar + ';' + LINE_BREAK +
                "AdaFruitGfxMenuRenderer renderer(&" + graphicsVar + ", DISPLAY_WIDTH, DISPLAY_HEIGHT);" + LINE_BREAK;
    }

    @Override
    public String getExportDefinitions() {
        return "extern AdaFruitGfxMenuRenderer renderer;";
    }

    @Override
    public String getSetupCode(String rootItem) {
        return "";
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
