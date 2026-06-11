/*
 * Copyright (c)  2016-2019 https://www.thecoderscorner.com (Dave Cherry).
 * This product is licensed under an Apache license, see the LICENSE file in the top-level directory.
 *
 */

package com.thecoderscorner.menu.editorui.generator.plugin;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class CodePluginItem {
    private String id;
    private String description;
    private String extendedDescription;
    private List<EmbeddedPlatform> supportedPlatforms;
    private List<String> requiredLibraries;
    private SubSystem subsystem;
    private String imageFileName;
    private String docsLink;
    private ThemeDescription themeDescription;
    private CodePluginConfig config;
    private List<CreatorProperty> properties;
    private List<CodeVariable> variables;
    private List<HeaderDefinition> includeFiles;
    private List<RequiredSourceFile> requiredSourceFiles;
    private List<FunctionDefinition> functions;
    private CodePluginManager manager;
    private JavaPluginItem javaImpl;

    public CodePluginItem() {
    }

    public List<CreatorProperty> getProperties() {
        if(javaImpl != null) javaImpl.getRequiredProperties();
        return properties;
    }

    public List<CodeVariable> getVariables() {
        if(javaImpl != null) return javaImpl.getVariables();
        return variables;
    }


    public List<HeaderDefinition> getIncludeFiles() {
        if(javaImpl != null) return javaImpl.getHeaderDefinitions();
        return includeFiles;
    }

    public List<RequiredSourceFile> getRequiredSourceFiles() {
        if(javaImpl != null) return javaImpl.getRequiredSourceFiles();
        return requiredSourceFiles;
    }

    public List<FunctionDefinition> getFunctions() {
        if(javaImpl != null) return javaImpl.getFunctions();
        return functions;
    }

    @Override
    public String toString() {
        return "CodePluginItem{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", extendedDescription='" + extendedDescription + '\'' +
                ", platforms=" + supportedPlatforms +
                ", subsystem=" + subsystem +
                ", imageFileName='" + imageFileName + '\'' +
                '}';
    }

    public CodePluginItem deepCopy() {
        var pluginCopy = new CodePluginItem();
        pluginCopy.setId(id);
        pluginCopy.setDescription(description);
        pluginCopy.setConfig(config);
        pluginCopy.setFunctions(functions);
        pluginCopy.setVariables(variables);
        pluginCopy.setDocsLink(docsLink);
        pluginCopy.setExtendedDescription(extendedDescription);
        pluginCopy.setImageFileName(imageFileName);
        pluginCopy.setIncludeFiles(includeFiles);
        pluginCopy.setRequiredLibraries(requiredLibraries);
        pluginCopy.setRequiredSourceFiles(requiredSourceFiles);
        pluginCopy.setSubsystem(subsystem);
        pluginCopy.setSupportedPlatforms(supportedPlatforms);
        pluginCopy.setThemeDescription(themeDescription);
        pluginCopy.setManager(manager);
        pluginCopy.setJavaImpl(javaImpl);
        pluginCopy.setProperties(properties.stream().map(prop -> new CreatorProperty(
                prop.getName(), prop.getDescription(), prop.getExtendedDescription(), prop.getLatestValue(), prop.getSubsystem(),
                prop.getPropType(), prop.getValidationRules(), prop.getApplicability())).toList());
        return pluginCopy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CodePluginItem that = (CodePluginItem) o;
        return Objects.equals(that.themeDescription, this.themeDescription) && Objects.equals(id, that.id) && Objects.equals(description, that.description) && Objects.equals(extendedDescription, that.extendedDescription) && Objects.equals(supportedPlatforms, that.supportedPlatforms) && Objects.equals(requiredLibraries, that.requiredLibraries) && subsystem == that.subsystem && Objects.equals(imageFileName, that.imageFileName) && Objects.equals(docsLink, that.docsLink) && Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, extendedDescription, supportedPlatforms, requiredLibraries, subsystem, imageFileName, docsLink, themeDescription, config);
    }

    public boolean isJavaPlugin() {
        return javaImpl != null;
    }

    public boolean isImagePng() {
        if(javaImpl != null) {
            return javaImpl.isImagePng();
        }
        return imageFileName != null && imageFileName.endsWith(".png");
    }
}
