/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Nutricherry LTD).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;

import java.util.List;
import java.util.stream.Collectors;

public class CodePluginItem {
    private String id;
    private String description;
    private String extendedDescription;
    private List<String> applicability;
    private SubSystem subsystem;
    private String imageFileName;
    private String codeCreatorClass;

    public CodePluginItem(String id, String description, String extendedDescription, List<String> applicability,
                          SubSystem subsystem, String imageFileName, String codeCreatorClass) {
        this.id = id;
        this.description = description;
        this.extendedDescription = extendedDescription;
        this.applicability = applicability;
        this.subsystem = subsystem;
        this.imageFileName = imageFileName;
        this.codeCreatorClass = codeCreatorClass;
    }

    public String getId() {
        return id;
    }

    public SubSystem getSubsystem() {
        return subsystem;
    }

    public String getDescription() {
        return description;
    }

    public String getExtendedDescription() {
        return extendedDescription;
    }

    public List<EmbeddedPlatform> getApplicability(EmbeddedPlatforms platforms) {
        return applicability.stream()
                .map(platforms::getEmbeddedPlatformFromId)
                .collect(Collectors.toList());
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public String getCodeCreatorClass() {
        return codeCreatorClass;
    }

    @Override
    public String toString() {
        return "CodePluginItem{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", extendedDescription='" + extendedDescription + '\'' +
                ", applicability=" + applicability +
                ", subsystem=" + subsystem +
                ", imageFileName='" + imageFileName + '\'' +
                ", codeCreatorClass='" + codeCreatorClass + '\'' +
                '}';
    }
}
