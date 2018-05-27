/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator;

import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.INPUT;

public abstract class AbstractCodeCreator implements EmbeddedCodeCreator {


    private static final CreatorProperty EMPTY = new CreatorProperty("", "", "-1", INPUT);

    @Override
    public String getExportDefinitions() {
        return properties().stream()
                .map(prop -> ("#define " + prop.getName() + " " + prop.getLatestValue()))
                .collect(Collectors.joining(LINE_BREAK)) + LINE_BREAK;
    }

    public CreatorProperty findPropertyValue(String name) {
        return properties().stream().filter(p->name.equals(p.getName())).findFirst().orElse(EMPTY);
    }

}
