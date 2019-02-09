package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;

import java.util.List;

public class CodePluginItem {
    private String id;
    private String description;
    private String extendedDescription;
    private List<EmbeddedPlatform> applicability;
    private SubSystem subsystem;
    private String imageFileName;
    private String codeCreatorClass;

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

    public List<EmbeddedPlatform> getApplicability() {
        return applicability;
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
