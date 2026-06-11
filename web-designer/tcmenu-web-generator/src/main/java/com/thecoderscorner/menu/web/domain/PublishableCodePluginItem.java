package com.thecoderscorner.menu.web.domain;

import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.CodePluginItem;
import com.thecoderscorner.menu.editorui.generator.plugin.EmbeddedPlatform;
import com.thecoderscorner.menu.editorui.generator.plugin.ThemeDescription;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Value
@AllArgsConstructor
public class PublishableCodePluginItem {
    String pluginGroup;
    String license;
    String vendor;
    String id;
    String description;
    String extendedDescription;
    List<EmbeddedPlatform> supportedPlatforms;
    List<String> requiredLibraries;
    SubSystem subsystem;
    String imageFileName;
    String docsLink;
    ThemeDescription themeDescription;
    List<PublishableCreationProperty> properties;
    Map<String, PublishablePropertyDescription> propertyDescriptions;

    public static PublishableCodePluginItem fromPlugin(CodePluginItem cpi) {

        var rulesByProperty = cpi.getProperties().stream()
                .map(PublishablePropertyDescription::new)
                .collect(Collectors.toMap(PublishablePropertyDescription::getName, Function.identity()));

        var pubProps = cpi.getProperties().stream().map(PublishableCreationProperty::new).toList();

        return new PublishableCodePluginItem(
                cpi.getConfig().getName(), cpi.getConfig().getLicense(), cpi.getConfig().getVendor(),
                cpi.getId(), cpi.getDescription(), cpi.getExtendedDescription(),
                cpi.getSupportedPlatforms(), cpi.getRequiredLibraries(), cpi.getSubsystem(),
                "/api/v1/generator/plugins/imgById/" + cpi.getId(),
                cpi.getDocsLink(), cpi.getThemeDescription(), pubProps, rulesByProperty);
    }
}
