/*
 * Copyright (c) 2018 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 */

package com.thecoderscorner.menu.editorui.project;

import com.thecoderscorner.menu.editorui.generator.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.display.DisplayType;
import com.thecoderscorner.menu.editorui.generator.input.InputType;
import com.thecoderscorner.menu.editorui.generator.remote.RemoteCapabilities;

import java.util.List;

public class CodeGeneratorOptions {
    private final EmbeddedPlatform embeddedPlatform;
    private final int lastDisplayKey;
    private final int lastInputKey;
    private final int lastRemoteKey;
    private final List<CreatorProperty> lastProperties;

    public CodeGeneratorOptions(EmbeddedPlatform embeddedPlatform, DisplayType displayType,
                                InputType inputType, RemoteCapabilities remoteCapabilities,
                                List<CreatorProperty> lastProperties) {
        this.embeddedPlatform = embeddedPlatform;
        this.lastDisplayKey = displayType.getKey();
        this.lastInputKey = inputType.getKey();
        this.lastRemoteKey = remoteCapabilities.getKey();
        this.lastProperties = lastProperties;
    }

    public EmbeddedPlatform getEmbeddedPlatform() {
        return embeddedPlatform;
    }

    public DisplayType getLastDisplayType() {
        return DisplayType.values.get(lastDisplayKey);
    }

    public InputType getLastInputType() {
        return InputType.values.get(lastInputKey);
    }

    public RemoteCapabilities getLastRemoteCapabilities() {
        return RemoteCapabilities.values.get(lastRemoteKey);
    }

    public List<CreatorProperty> getLastProperties() {
        return lastProperties;
    }
}
