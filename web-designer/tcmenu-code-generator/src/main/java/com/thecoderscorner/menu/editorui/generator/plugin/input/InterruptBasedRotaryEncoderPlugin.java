package com.thecoderscorner.menu.editorui.generator.plugin.input;

import com.thecoderscorner.menu.editorui.generator.applicability.EqualityApplicability;
import com.thecoderscorner.menu.editorui.generator.core.CreatorProperty;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.core.SubSystem;
import com.thecoderscorner.menu.editorui.generator.parameters.CodeParameter;
import com.thecoderscorner.menu.editorui.generator.plugin.*;
import com.thecoderscorner.menu.editorui.generator.validation.CannedPropertyValidators;
import com.thecoderscorner.menu.editorui.generator.validation.ChoiceDescription;

import java.util.List;

import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.INPUT;

public class InterruptBasedRotaryEncoderPlugin extends BaseJavaPluginItem {
    private final List<CreatorProperty> requiredProperties;
    private final CodePluginItem pluginItem;

    public InterruptBasedRotaryEncoderPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(SubSystem.INPUT, "/plugin/input/rotary-encoder.jpg");
        requiredProperties = List.of(
                CreatorProperty.optionalPin("ENCODER_APIN", "The A pin for the encoder", "The A pin for the encoder", "2", INPUT),
                CreatorProperty.optionalPin("ENCODER_BPIN", "The B pin for the encoder", "The B pin for the encoder", "3", INPUT),
                CreatorProperty.optionalPin("ENCODER_SEL_PIN", "Encoder Select/OK button", "The encoders select/OK button", "-1", INPUT),
                CreatorProperty.optionalPin("ENCODER_INT_PIN", "Separate interrupt pin", "Separate interrupt pin", "-1", INPUT, new EqualityApplicability("ENCODER_INT_MODE", "SINGLE", false)),
                new CreatorProperty("ENCODER_INT_MODE", "Interrupt/Timer mode", "Choose between the interrupt and timer mode", "BOTH", INPUT, CreatorProperty.PropType.VARIABLE,
                                    CannedPropertyValidators.choicesValidator(List.of(
                                            new ChoiceDescription("BOTH", "Attach interrupts to both A and B"),
                                            new ChoiceDescription("SINGLE", "Attach to separate pin that is A or B"),
                                            new ChoiceDescription("TIMER", "Use a hardware timer (at least 1Khz)")
                                    ), "BOTH"), ALWAYS_APPLICABLE),
                new CreatorProperty("ENCODER_TYPE", "Type of encoder", "Choose the encoder type that you are using", "FULL_CYCLE", INPUT, CreatorProperty.PropType.VARIABLE,
                        CannedPropertyValidators.choicesValidator(List.of(
                                new ChoiceDescription("FULL_CYCLE", "Full cycle (most common)"),
                                new ChoiceDescription("HALF_CYCLE", "Half cycle"),
                                new ChoiceDescription("QUARTER_CYCLE", "Quarter cycle")
                        ), "FULL_CYCLE"), ALWAYS_APPLICABLE)
        );
        var codePlugin = new CodePluginItem();
        codePlugin.setId("6c2be130-bf74-42ad-834c-ac2edd7b3f90");
        codePlugin.setDescription("Interrupt/Timer based rotary encoder (counts in ISR)");
        codePlugin.setExtendedDescription("An interrupt/timer based rotary encoder that counts inside the ISR, not affected by long running tasks. This is the recommended implementation to use.");
        codePlugin.setConfig(group.getConfig());
        codePlugin.setDocsLink("https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/tcmenu-plugins/encoder-switches-input-plugin/");
        codePlugin.setJavaImpl(this);
        codePlugin.setManager(manager);
        codePlugin.setProperties(requiredProperties);
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
        return requiredProperties;
    }

    @Override
    public List<FunctionDefinition> getFunctions() {
        String pinA = findPropOrFail("ENCODER_APIN");
        String pinB = findPropOrFail("ENCODER_BPIN");
        return List.of(
                FunctionDefinition.ofBuilderDeclaration("encBuild", "StateRotaryEncoderBuilder", List.of(
                        CodeParameter.unNamedValue("withEncoderPins(%s, %s)".formatted(pinA, pinB)),
                        interruptModeCodeParam(),
                        CodeParameter.unNamedValue("withCallback([](const int value) {menuMgr.valueChanged(value); })"),
                        CodeParameter.unNamedValue("withEncoderType(%s)".formatted(findPropOrFail("ENCODER_TYPE"))),
                        CodeParameter.unNamedValue("build()"))
                ),
                FunctionDefinition.ofRegCpp("onRelease", "switches", List.of(
                        CodeParameter.unNamedValue(findPropOrFail("ENCODER_SEL_PIN")),
                        CodeParameter.unNamedValue("[](pinid_t /*key*/, const bool held) { menuMgr.onMenuSelect(held); }"))
                ),
                FunctionDefinition.ofRegCpp("initWithoutInput","menuMgr", List.of(
                        CodeParameter.unNamedValue("&renderer"),
                        CodeParameter.unNamedValue("&${ROOT}"))
                )
        );
    }

    private CodeParameter interruptModeCodeParam() {
        var intMode = findPropOrFail("ENCODER_INT_MODE");
        if(intMode.equals("BOTH")) {
            return CodeParameter.unNamedValue("interruptOnBothPins()");
        } else if(intMode.equals("SINGLE")) {
            return CodeParameter.unNamedValue("interruptOnSinglePin(%s)".formatted(findPropOrFail("ENCODER_INT_PIN")));
        } else {
            return CodeParameter.unNamedValue("interruptOnTimer()");
        }
    }

    @Override
    public List<HeaderDefinition> getHeaderDefinitions() {
        return List.of(new HeaderDefinition("StateMachineEncoder.h", HeaderDefinition.HeaderType.GLOBAL, HeaderDefinition.PRIORITY_NORMAL, ALWAYS_APPLICABLE));
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
