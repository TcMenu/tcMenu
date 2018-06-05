/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.input;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.INPUT;

public class RotaryEncoderInputCreator extends AbstractCodeCreator {
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("ENCODER_PIN_A", "A pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_B", "B pin from rotary encoder", "0", INPUT),
            new CreatorProperty("ENCODER_PIN_OK", "OK button pin connector", "0", INPUT)
    );

    @Override
    public List<String> getIncludes() {
        return Collections.emptyList();
    }

    @Override
    public String getGlobalVariables() {
        return "";
    }

    @Override
    public String getSetupCode(String rootItem) {
        StringBuilder sb = new StringBuilder(256);
        return sb.append("    switches.initialise(ioUsingArduino());\n")
                .append("    menuMgr.initForEncoder(&renderer, &")
                .append(rootItem)
                .append(", ENCODER_PIN_A, ENCODER_PIN_B, ENCODER_PIN_OK);")
                .toString();
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
