/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;
import com.thecoderscorner.menu.pluginapi.AbstractCodeCreator;
import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.pluginapi.CreatorProperty.PropType.VARIABLE;
import static com.thecoderscorner.menu.pluginapi.SubSystem.REMOTE;

public class Rs232RemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final CurrentEditorProject project;
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device", REMOTE, TEXTUAL),
            new CreatorProperty("SERIAL_PORT", "Serial port variable name", "Serial", REMOTE, VARIABLE)
    );

    public Rs232RemoteCapabilitiesCreator(CurrentEditorProject project) {
        this.project = project;
    }

    @Override
    public List<String> getIncludes() {
        return Arrays.asList(
                "#include <RemoteConnector.h>",
                "#include \"SerialTransport.h\""
        );
    }

    @Override
    public List<String> getRequiredFiles() {
        return Arrays.asList("remotes/serial/SerialTransport.cpp","remotes/serial/SerialTransport.h");
    }

    @Override
    public String getGlobalVariables() {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();
        return "const char PROGMEM applicationName[] = \"" + deviceName + "\";" + LINE_BREAK;
    }

    @Override
    public String getExportDefinitions() {
        return super.getExportDefinitions() +
               "extern const char applicationName[];" + LINE_BREAK;
    }

    @Override
    public List<CreatorProperty> properties() {
        return creatorProperties;
    }

    @Override
    public String getSetupCode(String rootItem) {
        String serialPort = findPropertyValue("SERIAL_PORT").getLatestValue();
        return "    remoteServer.begin(&" + serialPort + ", applicationName);" + LINE_BREAK;
    }
}
