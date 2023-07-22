package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class Mcp23017DeviceExpander extends IoExpanderDefinition {
    private final int i2cAddress;
    private final String intPin;
    private final String name;

    public Mcp23017DeviceExpander(String name, int i2cAddress, String intPin) {
        this.i2cAddress = i2cAddress;
        this.intPin = intPin;
        this.name = name;
    }

    @Override
    public String getNicePrintableName() {
        return String.format("MCP23017(0x%02x, %s)", i2cAddress, intPin);
    }

    @Override
    public String getVariableName() {
        return "ioexp_" + name;
    }

    @Override
    public String getId() {
        return name;
    }

    public int getI2cAddress() {
        return i2cAddress;
    }

    public String getIntPin() {
        return intPin;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of(String.format("IoAbstractionRef ioexp_%s = ioFrom23017(0x%02x, ACTIVE_LOW_OPEN, %s);", name, i2cAddress, intPin));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of(String.format("extern IoAbstractionRef ioexp_%s;", name));
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
    @Override
    public String toString() {
        return "mcp23017:" + name + ":" + i2cAddress + ':' + intPin;
    }
}
