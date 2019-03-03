/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.arduino;

import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.SubSystem;
import com.thecoderscorner.menu.pluginapi.model.HeaderDefinition;
import com.thecoderscorner.menu.pluginapi.validation.CannedPropertyValidators;

import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.model.HeaderDefinition.PRIORITY_MAX;

public class ArduinoGlobalsCreator extends AbstractCodeCreator {

    private boolean progMem;

    public ArduinoGlobalsCreator(boolean progMem) {
        this.progMem = progMem;
    }

    @Override
    protected void initCreator(String root) {
        // and lastly add the tcMenu header file.
        addHeader(new HeaderDefinition("tcMenu.h", false, PRIORITY_MAX - 1));
    }

    @Override
    public List<CreatorProperty> properties() {
        if(progMem) {
            return List.of(
                    new CreatorProperty("TCMENU_USING_PROGMEM", "Defines when progmem define needed",
                            "true", SubSystem.INPUT, CreatorProperty.PropType.USE_IN_DEFINE,
                            CannedPropertyValidators.boolValidator()));
        }
        else {
            return Collections.emptyList();
        }
    }
}
