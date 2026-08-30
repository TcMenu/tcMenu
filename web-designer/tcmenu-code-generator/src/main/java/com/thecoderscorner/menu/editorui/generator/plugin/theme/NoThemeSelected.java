package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;

import java.util.List;

public class NoThemeSelected extends BaseJavaPluginItem {
    private final CodePluginItem codePlugin;

    public NoThemeSelected(InbuiltThemePlugins group, CodePluginManager manager) {
        super(SubSystem.THEME, "/plugin/theme/no-theme.png");
        codePlugin = new CodePluginItem();
        codePlugin.setId("2026a7f2-0d5b-43f5-9f98-4f0eacac4c0e");
        codePlugin.setDescription("No Theme Selected");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("No theme has been selected, please select a theme.");
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/themes/color-themes-for-all-display-sizes/");
        codePlugin.setJavaImpl(this);
        codePlugin.setThemeDescription(ThemeDescription.forTheme(ThemeDescription.ThemeMode.ANY));
        codePlugin.setManager(manager);
        codePlugin.setProperties(List.of());
        codePlugin.setSubsystem(SubSystem.THEME);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);

    }

    @Override
    public CodePluginItem getPlugin() {
        return codePlugin;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return List.of();
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        return List.of();
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of();
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        return List.of();
    }

    @Override
    public List<CodeVariable> getVariables() {
        return List.of();
    }
}
