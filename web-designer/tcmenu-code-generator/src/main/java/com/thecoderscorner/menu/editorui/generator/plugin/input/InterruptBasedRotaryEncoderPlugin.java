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

import static com.thecoderscorner.menu.editorui.generator.core.CreatorProperty.PropType.*;
import static com.thecoderscorner.menu.editorui.generator.core.SubSystem.INPUT;

public class InterruptBasedRotaryEncoderPlugin extends BaseJavaPluginItem {
    private final List<CreatorProperty> requiredProperties;
    private final CodePluginItem pluginItem;

    public InterruptBasedRotaryEncoderPlugin(JavaPluginGroup group, CodePluginManager manager) {
        super(SubSystem.INPUT, "/plugin/input/rotary-encoder.jpg");
        requiredProperties = List.of(
                separatorProperty("SEPARATOR_INTENC_MAIN", "Rotary Encoder settings"),
                CreatorProperty.optionalPin("ENCODER_APIN", "The A pin for the encoder", "The A pin for the encoder", "2", INPUT),
                CreatorProperty.optionalPin("ENCODER_BPIN", "The B pin for the encoder", "The B pin for the encoder", "3", INPUT),
                CreatorProperty.optionalPin("ENCODER_SEL_PIN", "Encoder Select/OK button", "The encoders select/OK button", "-1", INPUT),
                CreatorProperty.optionalPin("ENCODER_INT_PIN", "Separate interrupt pin", "Separate interrupt pin", "-1", INPUT, new EqualityApplicability("ENCODER_INT_MODE", "SINGLE", false)),
                new CreatorProperty("ENCODER_INT_MODE", "Interrupt/Timer mode", "Choose between the interrupt and timer mode", "BOTH", INPUT, VARIABLE,
                                    CannedPropertyValidators.choicesValidator(List.of(
                                            new ChoiceDescription("BOTH", "Attach interrupts to both A and B"),
                                            new ChoiceDescription("SINGLE", "Attach to separate pin that is A or B"),
                                            new ChoiceDescription("TIMER", "Use a hardware timer (at least 1Khz)")
                                    ), "BOTH"), ALWAYS_APPLICABLE),
                new CreatorProperty("ENCODER_TYPE", "Type of encoder", "Choose the encoder type that you are using", "FULL_CYCLE", INPUT, VARIABLE,
                        CannedPropertyValidators.choicesValidator(List.of(
                                new ChoiceDescription("FULL_CYCLE", "Full cycle (most common)"),
                                new ChoiceDescription("HALF_CYCLE", "Half cycle"),
                                new ChoiceDescription("QUARTER_CYCLE", "Quarter cycle")
                        ), "FULL_CYCLE"), ALWAYS_APPLICABLE),
                separatorProperty("SEPARATOR_SWITCHES_INTENC", "Button management / switches settings"),
                CreatorProperty.ofChoices("SWITCHES_INIT_MODE", "How to initialise button management", "Which mode to use to enable switches button management", INPUT, "POLLING", List.of(
                        new ChoiceDescription("NO_INIT", "Switches is already initialised"),
                        new ChoiceDescription("INTERRUPT", "Buttons will be interrupt driven"),
                        new ChoiceDescription("POLLING", "Buttons will be polled")
                )),
                CreatorProperty.boolProperty("SWITCHES_BUTTON_PULLUP", "The encoder button is pulled up", "The default button logic in switches will be set to pullup mode (active low)", true, INPUT)
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
                prepareSwitchesInit(),
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

    private FunctionDefinition prepareSwitchesInit() {
        var initMode = findPropOrFail("SWITCHES_INIT_MODE");
        var pullUp = Boolean.parseBoolean(findPropOrFail("SWITCHES_BUTTON_PULLUP"));

        if(initMode.equals("NO_INIT")) {
            return FunctionDefinition.ofRegCpp("ensureInitialized", "switches", List.of());
        } else {
            return FunctionDefinition.ofRegCpp("init", "switches", List.of(
                    CodeParameter.unNamedValue("asIoRef(internalDigitalDevice())"),
                    CodeParameter.unNamedValue(initMode.equals("INTERRUPT") ? "SWITCHES_NO_POLLING" : "SWITCHES_POLL_KEYS_ONLY"),
                    CodeParameter.unNamedValue(pullUp)
            ));
        }
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
