package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class InternalDeviceExpander extends IoExpanderDefinition {
    public static final String DEVICE_ID = "devicePins";

    @Override
    public String getNicePrintableName() {
        return "Device pins";
    }

    @Override
    public String getVariableName() {
        return "internalDigitalIo()";
    }

    @Override
    public String getId() {
        return DEVICE_ID;
    }

    @Override
    public String toString() {
        return "deviceIO:";
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.empty();
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
