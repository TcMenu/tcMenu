/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;

import java.util.List;

public class CodeGeneratorOptions {
    private String embeddedPlatform;
    private String lastDisplayUuid;
    private String lastInputUuid;
    private String lastRemoteUuid;
    private List<CreatorProperty> lastProperties;

    public CodeGeneratorOptions() {
        // for serialisation
    }

    public CodeGeneratorOptions(String embeddedPlatform, String displayTypeId,
                                String inputTypeId, String remoteCapabilitiesId,
                                List<CreatorProperty> lastProperties) {
        this.embeddedPlatform = embeddedPlatform;
        this.lastDisplayUuid = displayTypeId;
        this.lastInputUuid = inputTypeId;
        this.lastRemoteUuid = remoteCapabilitiesId;
        this.lastProperties = lastProperties;
    }

    public String getEmbeddedPlatform() {
        return embeddedPlatform;
    }

    public String getLastDisplayUuid() {
        return lastDisplayUuid;
    }

    public String getLastInputUuid() {
        return lastInputUuid;
    }

    public String getLastRemoteCapabilitiesUuid() {
        return lastRemoteUuid;
    }

    public List<CreatorProperty> getLastProperties() {
        return lastProperties;
    }
}
