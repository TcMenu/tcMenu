package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.parameters.FontMode;
import com.thecoderscorner.menu.editorui.generator.plugin.*;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.DISPLAY;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.INPUT;

public class NoInputPlugin extends BaseJavaPluginItem {
    private final CodePluginItem pluginItem;

    protected NoInputPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(DISPLAY, "/plugin/display/no-display.png");
        var codePlugin = new CodePluginItem();
        codePlugin.setId("89cd7f70-0457-4884-97c2-0db904ccb0ba");
        codePlugin.setDescription("No input required");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("Select this when you don't want tcMenu to render anything locally, I.E. no input/display on the embedded device.");
        codePlugin.setThemeDescription(ThemeDescription.monoWithFont(FontMode.ADAFRUIT));
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/customise-menu-input-display-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(List.of());
        codePlugin.setImageFileName("");
        codePlugin.setSubsystem(INPUT);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.allPlatforms);
        pluginItem = codePlugin;

    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return List.of();
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        return List.of(
                FunctionDefinition.ofRegCpp("initWithoutInput", "menuMgr", List.of(
                        CodeParameter.unNamedValue("&renderer"),
                        CodeParameter.unNamedValue("&${ROOT}")))
        );
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
