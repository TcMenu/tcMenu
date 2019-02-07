package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.pluginapi.EmbeddedCodeCreator;
import com.thecoderscorner.menu.pluginapi.EmbeddedPlatform;
import com.thecoderscorner.menu.pluginapi.SubSystem;

import java.util.List;
import java.util.Optional;

import static java.lang.System.Logger.Level.ERROR;

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

    @SuppressWarnings("unchecked")
    public Optional<EmbeddedCodeCreator> makeCreator() {
        try {
            Class<EmbeddedCodeCreator> clazz = (Class<EmbeddedCodeCreator>) Class.forName(codeCreatorClass);
            return Optional.of(clazz.getConstructor().newInstance());
        } catch (Exception e) {
            System.getLogger("CodePlugin").log(ERROR, "Plugin Class did not load " + description, e);
            return Optional.empty();
        }
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
