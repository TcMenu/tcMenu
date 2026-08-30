package com.thecoderscorner.menu.editorui.generator.plugin.theme;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.plugin.*;

import java.util.List;

public class ManualThemeSelected extends BaseJavaPluginItem {
    private final CodePluginItem codePlugin;

    public ManualThemeSelected(InbuiltThemePlugins group, CodePluginManager manager) {
        super(SubSystem.THEME, "/plugin/theme/no-theme.png");
        codePlugin = new CodePluginItem();
        codePlugin.setId("b186c809-d9ef-4ca8-9d4b-e4780a041ccc");
        codePlugin.setDescription("Provide your own theme manually");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Use this option to provide your own theme using theme builder instead of starting with one of ours");
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
