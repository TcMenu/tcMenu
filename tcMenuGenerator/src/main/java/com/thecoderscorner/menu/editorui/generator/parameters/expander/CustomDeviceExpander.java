package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class CustomDeviceExpander extends IoExpanderDefinition {
    private String varName;

    public CustomDeviceExpander(String varName) {
        this.varName = varName;
    }

    @Override
    public String getNicePrintableName() {
        return "Custom IO: " + varName;
    }

    @Override
    public String getVariableName() {
        return varName;
    }

    @Override
    public String getId() {
        return varName;
    }

    @Override
    public String toString() {
        return "customIO:" + varName;
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
        return Optional.of("extern IoExpanderRef " + varName + ";");
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstraction.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
