/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.pluginapi.CreatorProperty;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;

import java.util.List;

public class CodeGeneratorOptions {
    private final EmbeddedPlatform embeddedPlatform;
    private final String lastDisplayUuid;
    private final String lastInputUuid;
    private final String lastRemoteUuid;
    private final List<CreatorProperty> lastProperties;

    public CodeGeneratorOptions(EmbeddedPlatform embeddedPlatform, String displayTypeId,
                                String inputTypeId, String remoteCapabilitiesId,
                                List<CreatorProperty> lastProperties) {
        this.embeddedPlatform = embeddedPlatform;
        this.lastDisplayUuid = displayTypeId;
        this.lastInputUuid = inputTypeId;
        this.lastRemoteUuid = remoteCapabilitiesId;
        this.lastProperties = lastProperties;
    }

    public EmbeddedPlatform getEmbeddedPlatform() {
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
