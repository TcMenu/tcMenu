/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem.INPUT;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public abstract class AbstractCodeCreator implements EmbeddedCodeCreator {


    private static final CreatorProperty EMPTY = new CreatorProperty("", "", "-1", INPUT);

    @Override
    public String getExportDefinitions() {
        return properties().stream()
                .filter(prop -> prop.getPropType() == CreatorProperty.PropType.USE_IN_DEFINE)
                .map(prop -> ("#define " + prop.getName() + " " + prop.getLatestValue()))
                .collect(Collectors.joining(LINE_BREAK)) + LINE_BREAK;
    }

    @Override
    public List<String> getRequiredFiles() {
        return Collections.emptyList();
    }

    public CreatorProperty findPropertyValue(String name) {
        return properties().stream().filter(p->name.equals(p.getName())).findFirst().orElse(EMPTY);
    }

    protected boolean getBooleanFromProperty(String propName) {
        CreatorProperty prop = findPropertyValue(propName);
        return Boolean.valueOf(prop.getLatestValue());
    }
}
