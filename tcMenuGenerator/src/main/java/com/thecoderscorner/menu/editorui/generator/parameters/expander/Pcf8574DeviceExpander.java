package com.thecoderscorner.menu.editorui.generator.parameters.expander;

import com.thecoderscorner.menu.editorui.generator.applicability.AlwaysApplicable;
import com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition;
import com.thecoderscorner.menu.editorui.generator.parameters.IoExpanderDefinition;

import java.util.Optional;

import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.*;
import static com.thecoderscorner.menu.editorui.generator.core.HeaderDefinition.HeaderType.*;

public class Pcf8574DeviceExpander extends IoExpanderDefinition {
    private final int i2cAddress;
    private final String intPin;
    private final String name;
    private final boolean inverted;

    public Pcf8574DeviceExpander(String name, int i2cAddress, String intPin, boolean inverted) {
        this.i2cAddress = i2cAddress;
        this.intPin = intPin;
        this.name = name;
        this.inverted = inverted;
    }

    @Override
    public String getNicePrintableName() {
        var possibleInvert = inverted ? "!" : "";
        return String.format("%sPCF8574(0x%02x, %s)",  possibleInvert, i2cAddress, intPin);
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

    public boolean isInverted() {
        return inverted;
    }

    @Override
    public String toString() {
        return "pcf8574:" + name + ":" + i2cAddress + ":" + intPin + ":" + inverted;
    }

    @Override
    public Optional<String> generateCode() {
        return Optional.empty();
    }

    @Override
    public Optional<String> generateGlobal() {
        var invertedCode = inverted ? ", true" : "";
        return Optional.of(String.format("IoAbstractionRef ioexp_%s = ioFrom8574(0x%02x, %s%s);", name, i2cAddress, intPin, invertedCode));
    }

    @Override
    public Optional<String> generateExport() {
        return Optional.of(String.format("extern IoAbstractionRef ioexp_%s;", name));
    }

    @Override
    public Optional<HeaderDefinition> generateHeader() {
        return Optional.of(new HeaderDefinition("IoAbstractionWire.h", GLOBAL, PRIORITY_NORMAL, new AlwaysApplicable()));
    }
}
