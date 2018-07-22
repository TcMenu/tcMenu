/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.EmbeddedCodeCreator;
import com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.ui.CreatorProperty.SubSystem.REMOTE;

public class NoRemoteCapability extends AbstractCodeCreator {
    private List<CreatorProperty> creatorProperties = new ArrayList<>(Collections.singletonList(
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device", REMOTE, TEXTUAL)
    ));

    @Override
    public List<String> getIncludes() {
        return Collections.singletonList("#include \"RemoteConnector.h\"");
    }

    @Override
    public String getGlobalVariables() {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();
        return "const char PROGMEM applicationName[] = \"" + deviceName + "\";" + LINE_BREAK;
    }

    @Override
    public String getExportDefinitions() {
        return  "extern const char applicationName[];" + LINE_BREAK;
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
