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

public class UpDownOkInputCreator extends AbstractCodeCreator {

    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("ENCODER_UP_PIN", "Up button pin connector", "0", INPUT),
            new CreatorProperty("ENCODER_DOWN_PIN", "Down button pin connector", "0", INPUT),
            new CreatorProperty("ENCODER_OK_PIN", "OK button pin connector", "0", INPUT)
    );

    @Override
    public String getSetupCode(String rootItem) {
        StringBuilder sb = new  StringBuilder(256);
        return sb.append("    switches.initialise(ioUsingArduino());\n")
                 .append("    menuMgr.initForUpDownOk(&renderer, &")
                 .append(rootItem)
                 .append(", ENCODER_PIN_UP, ENCODER_PIN_DOWN, ENCODER_BUTTON_PIN);")
                 .toString();
    }

    @Override
    public List<String> getIncludes() {
        return Collections.emptyList();
    }

    @Override
    public String getGlobalVariables() {
        return null;
    }

    @Override
    public String getExportDefinitions() {
        return super.getExportDefinitions();
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }
}
