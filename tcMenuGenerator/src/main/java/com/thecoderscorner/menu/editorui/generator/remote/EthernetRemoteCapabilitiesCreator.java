/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.generator.remote;

import com.thecoderscorner.menu.editorui.generator.AbstractCodeCreator;
import com.thecoderscorner.menu.editorui.generator.CreatorProperty;
import com.thecoderscorner.menu.editorui.project.CurrentEditorProject;

import java.util.Arrays;
import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.PropType.TEXTUAL;
import static com.thecoderscorner.menu.editorui.generator.CreatorProperty.SubSystem.REMOTE;
import static com.thecoderscorner.menu.editorui.generator.arduino.ArduinoItemGenerator.LINE_BREAK;

public class EthernetRemoteCapabilitiesCreator extends AbstractCodeCreator {
    private final CurrentEditorProject project;
    private final List<CreatorProperty> creatorProperties = List.of(
            new CreatorProperty("LISTEN_PORT", "Port to listen on", "3333", REMOTE),
            new CreatorProperty("DEVICE_NAME", "Name of this device", "New Device", REMOTE, TEXTUAL)
    );

    public EthernetRemoteCapabilitiesCreator(CurrentEditorProject project) {
        this.project = project;
    }

    @Override
    public List<String> getIncludes() {
        return Arrays.asList(
                "#include <RemoteConnector.h>",
                "#include \"EthernetTransport.h\""
        );
    }

    @Override
    public List<String> getRequiredFiles() {
        return Arrays.asList("remotes/ethernet/EthernetTransport.cpp","remotes/ethernet/EthernetTransport.h");
    }

    @Override
    public String getGlobalVariables() {
        String deviceName = findPropertyValue("DEVICE_NAME").getLatestValue();
        return "const char PROGMEM applicationName[] = \"" +  deviceName + "\";" + LINE_BREAK +
               "EthernetServer server(LISTEN_PORT);" + LINE_BREAK;
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
        return "    remoteServer.begin(&server, applicationName);" + LINE_BREAK;
    }
}
