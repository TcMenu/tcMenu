package com.thecoderscorner.menu.editorui.generator.plugin.display;

import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import javafx.scene.image.Image;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType;

public class DfRobotDisplayPluginImpl extends BaseJavaPluginItem {
    private final CodePluginItem pluginItem;
    private final List<CreatorProperty> requiredProperties = List.of(
            CommonLCDPluginHelper.unoOrFullProperty(),
            CommonDisplayPluginHelper.updatesPerSecond()
    );

    public DfRobotDisplayPluginImpl(JavaPluginGroup group, CodePluginManager manager) {
        var codePlugin = new CodePluginItem();
        codePlugin.setId("bcd5fe34-9e9f-4fcb-9edf-f4e3caca0674");
        codePlugin.setDescription("DFRobot LCD Shield plugin");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setExtendedDescription("This plugin provides support for the common DFRobot LCD shields for Arduino.");
        codePlugin.setDocsLink("https://tcmenu.github.io/documentation/arduino-libraries/tc-menu/tcmenu-plugins/dfrobot-input-display-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
        codePlugin.setSubsystem(SubSystem.DISPLAY);
        codePlugin.setSupportedPlatforms(PluginEmbeddedPlatformsImpl.arduinoPlatforms);
        pluginItem = codePlugin;
    }

    @Override
    public CodePluginItem getPlugin() {
        return pluginItem;
    }

    @Override
    public List<CreatorProperty> getRequiredProperties() {
        return requiredProperties;
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        return List.of(
                new FunctionDefinition("begin", "lcd", false, false, List.of(
                        CodeParameter.unNamedValue(16),
                        CodeParameter.unNamedValue(2)
                ), ALWAYS_APPLICABLE),
                basicUpdatesPerSecond(),
                new FunctionDefinition("configureBacklightPin", "lcd", false, false, List.of(
                        CodeParameter.unNamedValue(10)
                ), ALWAYS_APPLICABLE),
                new FunctionDefinition("backlight", "lcd", false, false, List.of(), ALWAYS_APPLICABLE)
        );
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(
                new HeaderDefinition("LiquidCrystalIO.h", HeaderType.GLOBAL, 0, ALWAYS_APPLICABLE),
                new HeaderDefinition("tcMenuLiquidCrystal.h", HeaderType.SOURCE, 1, ALWAYS_APPLICABLE)
        );
    }

    @Override
    public List<RequiredSourceFile> getRequiredSourceFiles() {
        var unoOrFull = findPropOrFail("UNO_OR_FULL");
        boolean isUno = unoOrFull.equals("unoLcd");
        return List.of(
                new RequiredSourceFile("/tcMenuLiquidCrystal.cpp", CommonLCDPluginHelper.getSourceCode(isUno), List.of(), false),
                new RequiredSourceFile("/tcMenuLiquidCrystal.h", CommonLCDPluginHelper.getHeaderCode(isUno), List.of(), false)
        );
    }

    @Override
    public List<CodeVariable> getVariables() {
        return List.of(
                new CodeVariable("lcd", "LiquidCrystal", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                        CodeParameter.unNamedValue(8), CodeParameter.unNamedValue(9),
                        CodeParameter.unNamedValue(4), CodeParameter.unNamedValue(5),
                        CodeParameter.unNamedValue(6), CodeParameter.unNamedValue(7)
                ), ALWAYS_APPLICABLE),
                new CodeVariable("renderer", "LiquidCrystalRenderer", VariableDefinitionMode.VARIABLE_AND_EXPORT, false, false, false, List.of(
                        CodeParameter.unNamedValue("lcd"),
                        CodeParameter.unNamedValue(16),
                        CodeParameter.unNamedValue(2)
                ), ALWAYS_APPLICABLE)
        );
    }

    @Override
    public Optional<Image> getImage() {
        return Optional.of(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/plugin/display/DfRobotShield.jpg"))));
    }
}