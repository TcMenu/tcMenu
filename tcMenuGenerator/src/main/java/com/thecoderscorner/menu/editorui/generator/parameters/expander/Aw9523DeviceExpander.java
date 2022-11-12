package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.GLOBAL;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.PRIORITY_NORMAL;

public class Aw9523DeviceExpander extends IoExpanderDefinition  {
    private final int i2cAddress;
    private final int intPin;
    private final String name;

    public Aw9523DeviceExpander(String name, int i2cAddress, int intPin) {
        this.i2cAddress = i2cAddress;
        this.intPin = intPin;
        this.name = name;
    }

    @Override
    public String getNicePrintableName() {
        return String.format("AW9523(0x%02x, %d)", i2cAddress, intPin);
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

    public int getIntPin() {
        return intPin;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        return Optional.of(String.format("""
                AW9523IoAbstraction iodev_%s(0x%02x, %d);
                IoAbstractionRef ioexp_%1$s = &iodev_%1$s;""", name, i2cAddress, intPin));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of(String.format("""
                extern AW9523IoAbstraction iodev_%1$s;
                extern IoAbstractionRef ioexp_%s;""", name));
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
    @Override
    public String toString() {
        return "aw9523:" + name + ":" + i2cAddress + ':' + intPin;
    }
}
